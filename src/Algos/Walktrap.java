package Algos;

import Graphs.IGraph;
import java.util.*;
import java.util.Arrays;

/**
 * Walktrap algorithm implementation.
 *
 * This implementation assumes that the graph's nodes are convertible to integers
 * (i.e. we create a mapping from nodes T to indices 0..N-1).
 *
 * It returns a WalktrapResult that contains:
 *  - partitions: a list of partitions (each a set of current community IDs) at each merging step.
 *  - communities: a map from community ID to Community (which holds community info such as
 *    the weighted probability vector, vertices, internal and total weights).
 *  - deltaSigmas: the delta sigma values recorded at each merge.
 *  - modularities: the modularity for each partition.
 */
public class Walktrap<T> {
    private final IGraph<T> graph;
    private final int t;               // random walk length
    private final boolean addSelfEdges;
    private final boolean verbose;

    // These fields are computed from the graph.
    private int N;                                  // number of nodes
    private Map<T, Integer> nodeToIndex;            // map from node to integer index
    private List<T> indexToNode;                    // list of nodes indexed by integer
    private double[][] A;                           // adjacency matrix (N x N)
    private double[][] P;                           // transition matrix (N x N)
    private double[][] P_t;                         // P raised to power t (N x N)
    private double[] Dx;                            // diagonal matrix (stored as 1D array: Dx[i] = d_i^-0.5)
    private double G_total_weight;                  // total weight of all (non-self) edges

    // Data structures for the agglomerative merge.
    private Map<Integer, Community> communities;    // maps community id to Community
    private int communityCount;                     // used to assign new community IDs

    // Recording partitions (each partition is the set of current community IDs),
    // delta sigmas at each merge, and modularity values.
    private List<Set<Integer>> partitions;
    private List<Double> deltaSigmas;
    private List<Double> modularities;

    public Walktrap(IGraph<T> graph, int t, boolean addSelfEdges, boolean verbose) {
        this.graph = graph;
        this.t = t;
        this.addSelfEdges = addSelfEdges;
        this.verbose = verbose;
    }

    /**
     * Runs the Walktrap algorithm.
     *
     * @return a WalktrapResult containing partitions, communities, deltaSigmas, and modularities.
     */
    public WalktrapResult run() {
        // Optionally add self edges.
        if (addSelfEdges) {
            for (T v : graph.getNodes()) {
                graph.addEdge("self", v, v);
            }
        }

        // --- Build the matrix representation ---
        // Create node-to-index mapping.
        Set<T> nodes = new HashSet<>();
        graph.getNodes().forEach(nodes::add);
        N = nodes.size();
        nodeToIndex = new HashMap<>();
        indexToNode = new ArrayList<>(N);
        int idx = 0;
        for (T node : nodes) {
            nodeToIndex.put(node, idx);
            indexToNode.add(node);
            idx++;
        }

        // Build the adjacency matrix A.
        A = new double[N][N];
        for (T u : nodes) {
            int i = nodeToIndex.get(u);
            for (T v : graph.getNeighbors(u)) {
                int j = nodeToIndex.get(v);
                A[i][j] = 1.0;
            }
        }

        // Build transition matrix P and diagonal matrix Dx.
        P = new double[N][N];
        Dx = new double[N];
        for (int i = 0; i < N; i++) {
            double degree = 0.0;
            for (int j = 0; j < N; j++) {
                degree += A[i][j];
            }
            if (degree == 0) degree = 1.0; // avoid division by zero
            for (int j = 0; j < N; j++) {
                P[i][j] = A[i][j] / degree;
            }
            Dx[i] = Math.pow(degree, -0.5);
        }

        // Compute P_t = P^t.
        P_t = matrixPower(P, t);

        // Compute G_total_weight = (sum_{i,j} A[i][j] - N) / 2.
        double sumA = 0.0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sumA += A[i][j];
            }
        }
        G_total_weight = (sumA - N) / 2.0;

        // --- Initialize communities ---
        communities = new HashMap<>();
        communityCount = N;
        for (int i = 0; i < N; i++) {
            communities.put(i, new Community(i));
        }

        // --- Build initial merge candidates ---
        PriorityQueue<MergeCandidate> minHeap = new PriorityQueue<>();
        // For every edge (i,j) with i != j and A[i][j]==1.
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i != j && A[i][j] == 1.0) {
                    double ds = computeDeltaSigma(i, j);
                    minHeap.add(new MergeCandidate(i, j, ds));
                    communities.get(i).adjComs.put(j, ds);
                    communities.get(j).adjComs.put(i, ds);
                }
            }
        }

        // --- Set up recording of partitions, delta sigmas, modularities ---
        partitions = new ArrayList<>();
        deltaSigmas = new ArrayList<>();
        modularities = new ArrayList<>();

        // Initial partition: each vertex (and thus community) is separate.
        Set<Integer> initialPartition = new HashSet<>();
        for (int i = 0; i < N; i++) {
            initialPartition.add(i);
        }
        partitions.add(initialPartition);

        // Compute modularity for initial partition.
        double mod0 = 0.0;
        for (int cid : initialPartition) {
            mod0 += communities.get(cid).modularity();
        }
        modularities.add(mod0);
        if (verbose) {
            System.out.println("Partition 0: " + initialPartition);
            System.out.println("Q(0) = " + mod0);
        }

        // --- Agglomerative merging ---
        // For k = 1 to N-1, merge two communities at each step.
        for (int k = 1; k < N; k++) {
            Set<Integer> currentPartition = partitions.get(k - 1);
            MergeCandidate candidate = null;
            // Find a candidate whose both communities are still active.
            while (!minHeap.isEmpty()) {
                candidate = minHeap.poll();
                if (currentPartition.contains(candidate.comm1) && currentPartition.contains(candidate.comm2)) {
                    break;
                }
                candidate = null;
            }
            if (candidate == null) break;

            deltaSigmas.add(candidate.deltaSigma);

            // Merge candidate.comm1 and candidate.comm2 into a new community.
            int newCommId = communityCount++;
            Community newCommunity = new Community(newCommId, communities.get(candidate.comm1), communities.get(candidate.comm2));
            communities.put(newCommId, newCommunity);

            // Create a new partition: remove the merged community IDs and add the new community.
            Set<Integer> newPartition = new HashSet<>(currentPartition);
            newPartition.remove(candidate.comm1);
            newPartition.remove(candidate.comm2);
            newPartition.add(newCommId);
            partitions.add(newPartition);

            // Update the min-heap: for every community adjacent to the new community, compute new delta sigma.
            for (Integer other : newCommunity.adjComs.keySet()) {
                if (!newPartition.contains(other)) continue;
                double ds;
                // If 'other' was adjacent to both merged communities, apply Theorem 4.
                if (communities.get(candidate.comm1).adjComs.containsKey(other) &&
                        communities.get(candidate.comm2).adjComs.containsKey(other)) {
                    double ds1 = communities.get(candidate.comm1).adjComs.get(other);
                    double ds2 = communities.get(candidate.comm2).adjComs.get(other);
                    ds = ((communities.get(candidate.comm1).size + communities.get(other).size) * ds1 +
                            (communities.get(candidate.comm2).size + communities.get(other).size) * ds2 -
                            communities.get(other).size * candidate.deltaSigma)
                            / (newCommunity.size + communities.get(other).size);
                } else {
                    ds = computeDeltaSigmaForCommunities(newCommunity, communities.get(other));
                }
                minHeap.add(new MergeCandidate(newCommId, other, ds));
                newCommunity.adjComs.put(other, ds);
                communities.get(other).adjComs.put(newCommId, ds);
            }

            // Compute modularity for the new partition.
            double mod = 0.0;
            for (int cid : newPartition) {
                mod += communities.get(cid).modularity();
            }
            modularities.add(mod);
            if (verbose) {
                System.out.println("Partition " + k + ": " + newPartition);
                System.out.println("\tMerging " + candidate.comm1 + " + " + candidate.comm2 + " --> " + newCommId);
                System.out.println("\tQ(" + k + ") = " + mod);
                System.out.println("\tdelta_sigma = " + candidate.deltaSigma);
            }
        }

        return new WalktrapResult(partitions, communities, deltaSigmas, modularities);
    }

    // --- Matrix helper methods ---
    private double[][] matrixPower(double[][] M, int p) {
        int n = M.length;
        double[][] result = identityMatrix(n);
        for (int i = 0; i < p; i++) {
            result = multiplyMatrices(result, M);
        }
        return result;
    }

    private double[][] identityMatrix(int n) {
        double[][] I = new double[n][n];
        for (int i = 0; i < n; i++) {
            I[i][i] = 1.0;
        }
        return I;
    }

    private double[][] multiplyMatrices(double[][] X, double[][] Y) {
        int n = X.length;
        double[][] Z = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += X[i][k] * Y[k][j];
                }
                Z[i][j] = sum;
            }
        }
        return Z;
    }

    // --- Delta sigma computation ---
    // For two single-vertex communities (indexed by u and v).
    private double computeDeltaSigma(int u, int v) {
        double sumSq = 0.0;
        for (int j = 0; j < N; j++) {
            double diff = Dx[j] * P_t[u][j] - Dx[j] * P_t[v][j];
            sumSq += diff * diff;
        }
        return (0.5 / N) * sumSq;
    }

    // For two (possibly merged) communities using their P_c vectors.
    private double computeDeltaSigmaForCommunities(Community C1, Community C2) {
        double sumSq = 0.0;
        for (int j = 0; j < N; j++) {
            double diff = Dx[j] * C1.P_c[j] - Dx[j] * C2.P_c[j];
            sumSq += diff * diff;
        }
        return sumSq * (C1.size * C2.size) / ((C1.size + C2.size) * N);
    }

    // --- Inner classes ---

    // Merge candidate used in the min-heap.
    private class MergeCandidate implements Comparable<MergeCandidate> {
        int comm1;
        int comm2;
        double deltaSigma;

        public MergeCandidate(int comm1, int comm2, double deltaSigma) {
            this.comm1 = comm1;
            this.comm2 = comm2;
            this.deltaSigma = deltaSigma;
        }

        @Override
        public int compareTo(MergeCandidate other) {
            return Double.compare(this.deltaSigma, other.deltaSigma);
        }
    }

    // Community class representing a group of vertices.
    public class Community {
        int id;
        int size;
        double[] P_c;                      // probability vector (length N)
        Map<Integer, Double> adjComs;        // mapping from adjacent community id to delta sigma
        Set<Integer> vertices;               // set of vertex indices in this community
        double internalWeight;
        double totalWeight;

        // Constructor for a single-vertex community.
        public Community(int id) {
            this.id = id;
            this.size = 1;
            // For a single vertex, use the corresponding row of P_t.
            this.P_c = Arrays.copyOf(P_t[id], N);
            this.adjComs = new HashMap<>();
            this.vertices = new HashSet<>();
            this.vertices.add(id);
            this.internalWeight = 0.0;
            // totalWeight = (degree of vertex excluding self-edge) / 2.
            int count = 0;
            for (int j = 0; j < N; j++) {
                if (j != id && A[id][j] == 1.0) count++;
            }
            this.totalWeight = count / 2.0;
        }

        // Constructor for merging two communities.
        public Community(int newId, Community C1, Community C2) {
            this.id = newId;
            this.size = C1.size + C2.size;
            this.P_c = new double[N];
            // Weighted average of probability vectors.
            for (int j = 0; j < N; j++) {
                this.P_c[j] = (C1.size * C1.P_c[j] + C2.size * C2.P_c[j]) / this.size;
            }
            // Merge adjacent communities (simply combine the maps).
            this.adjComs = new HashMap<>();
            this.adjComs.putAll(C1.adjComs);
            for (Map.Entry<Integer, Double> entry : C2.adjComs.entrySet()) {
                this.adjComs.put(entry.getKey(), entry.getValue());
            }
            // Remove self-references.
            this.adjComs.remove(C1.id);
            this.adjComs.remove(C2.id);
            // Merge vertex sets.
            this.vertices = new HashSet<>();
            this.vertices.addAll(C1.vertices);
            this.vertices.addAll(C2.vertices);
            // Compute weight between C1 and C2.
            double weightBetween = 0.0;
            for (int v1 : C1.vertices) {
                for (int j = 0; j < N; j++) {
                    if (A[v1][j] == 1.0 && C2.vertices.contains(j)) {
                        weightBetween += 1.0;
                    }
                }
            }
            this.internalWeight = C1.internalWeight + C2.internalWeight + weightBetween;
            this.totalWeight = C1.totalWeight + C2.totalWeight;
        }

        // Compute modularity for this community.
        public double modularity() {
            return (internalWeight - (totalWeight * totalWeight / G_total_weight)) / G_total_weight;
        }
    }

    // Result object to encapsulate the output of the algorithm.
    public class WalktrapResult {
        public final List<Set<Integer>> partitions;
        public final Map<Integer, Community> communities;
        public final List<Double> deltaSigmas;
        public final List<Double> modularities;

        public WalktrapResult(List<Set<Integer>> partitions, Map<Integer, Community> communities,
                              List<Double> deltaSigmas, List<Double> modularities) {
            this.partitions = partitions;
            this.communities = communities;
            this.deltaSigmas = deltaSigmas;
            this.modularities = modularities;
        }
    }
}
