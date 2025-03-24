package Algos;

import Graphs.IGraph;
import java.util.*;
import java.util.Arrays;

/**
 * A java implementation of: <a href="https://github.com/jancio/Computing-Communities-in-Large-Networks-Using-Random-Walks">Jancio's Walktrap Implementation</a>
 *
 * @param <T> the type of nodes in the graph.
 */
public class Walktrap<T> {
    private final IGraph<T> graph;
    private final int t;
    private final boolean addSelfEdges;
    private final boolean verbose;

    private int N;
    private double[][] A;
    private double[][] P;
    private double[][] P_t;
    private double[] Dx;
    private double G_total_weight;

    public Walktrap(IGraph<T> graph, int t, boolean addSelfEdges, boolean verbose) {
        this.graph = graph;
        this.t = t;
        this.addSelfEdges = addSelfEdges;
        this.verbose = verbose;
    }

    public WalktrapResult run() {
        if (addSelfEdges) {
            for (T v : graph.getNodes()) {
                graph.addRelationship("self", v, v);
            }
        }

        Set<T> nodes = new HashSet<>();
        graph.getNodes().forEach(nodes::add);
        N = nodes.size();
        Map<T, Integer> nodeToIndex = new HashMap<>();
        int idx = 0;
        for (T node : nodes) {
            nodeToIndex.put(node, idx);
            idx++;
        }

        A = new double[N][N];
        for (T u : nodes) {
            int i = nodeToIndex.get(u);
            for (T v : graph.getRelationships(u)) {
                int j = nodeToIndex.get(v);
                A[i][j] = 1.0;
            }
        }

        P = new double[N][N];
        Dx = new double[N];
        for (int i = 0; i < N; i++) {
            double degree = 0.0;
            for (int j = 0; j < N; j++) {
                degree += A[i][j];
            }
            if (degree == 0) degree = 1.0;
            for (int j = 0; j < N; j++) {
                P[i][j] = A[i][j] / degree;
            }
            Dx[i] = Math.pow(degree, -0.5);
        }

        P_t = matrixPower(P, t);

        double sumA = 0.0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sumA += A[i][j];
            }
        }
        G_total_weight = (sumA - N) / 2.0;

        Map<Integer, Community> communities = new HashMap<>();
        int communityCount = N;
        for (int i = 0; i < N; i++) {
            communities.put(i, new Community(i));
        }

        PriorityQueue<MergeCandidate> minHeap = new PriorityQueue<>();
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

        List<Set<Integer>> partitions = new ArrayList<>();
        List<Double> deltaSigmas = new ArrayList<>();
        List<Double> modularities = new ArrayList<>();

        Set<Integer> initialPartition = new HashSet<>();
        for (int i = 0; i < N; i++) {
            initialPartition.add(i);
        }
        partitions.add(initialPartition);

        double mod0 = 0.0;
        for (int cid : initialPartition) {
            mod0 += communities.get(cid).modularity();
        }
        modularities.add(mod0);
        if (verbose) {
            System.out.println("Partition 0: " + initialPartition);
            System.out.println("Q(0) = " + mod0);
        }

        for (int k = 1; k < N; k++) {
            Set<Integer> currentPartition = partitions.get(k - 1);
            MergeCandidate candidate = null;
            while (!minHeap.isEmpty()) {
                candidate = minHeap.poll();
                if (currentPartition.contains(candidate.comm1) && currentPartition.contains(candidate.comm2)) {
                    break;
                }
                candidate = null;
            }
            if (candidate == null) break;

            deltaSigmas.add(candidate.deltaSigma);

            int newCommId = communityCount++;
            Community newCommunity = new Community(newCommId, communities.get(candidate.comm1), communities.get(candidate.comm2));
            communities.put(newCommId, newCommunity);

            Set<Integer> newPartition = new HashSet<>(currentPartition);
            newPartition.remove(candidate.comm1);
            newPartition.remove(candidate.comm2);
            newPartition.add(newCommId);
            partitions.add(newPartition);

            for (Integer other : newCommunity.adjComs.keySet()) {
                if (!newPartition.contains(other)) continue;
                double ds;
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

    private double computeDeltaSigma(int u, int v) {
        double sumSq = 0.0;
        for (int j = 0; j < N; j++) {
            double diff = Dx[j] * P_t[u][j] - Dx[j] * P_t[v][j];
            sumSq += diff * diff;
        }
        return (0.5 / N) * sumSq;
    }

    private double computeDeltaSigmaForCommunities(Community C1, Community C2) {
        double sumSq = 0.0;
        for (int j = 0; j < N; j++) {
            double diff = Dx[j] * C1.P_c[j] - Dx[j] * C2.P_c[j];
            sumSq += diff * diff;
        }
        return sumSq * (C1.size * C2.size) / ((C1.size + C2.size) * N);
    }

    private static class MergeCandidate implements Comparable<MergeCandidate> {
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

    public class Community {
        int id;
        int size;
        double[] P_c;
        Map<Integer, Double> adjComs;
        public Set<Integer> vertices;
        double internalWeight;
        double totalWeight;

        public Community(int id) {
            this.id = id;
            this.size = 1;
            this.P_c = Arrays.copyOf(P_t[id], N);
            this.adjComs = new HashMap<>();
            this.vertices = new HashSet<>();
            this.vertices.add(id);
            this.internalWeight = 0.0;
            int count = 0;
            for (int j = 0; j < N; j++) {
                if (j != id && A[id][j] == 1.0) count++;
            }
            this.totalWeight = count / 2.0;
        }

        public Community(int newId, Community C1, Community C2) {
            this.id = newId;
            this.size = C1.size + C2.size;
            this.P_c = new double[N];
            for (int j = 0; j < N; j++) {
                this.P_c[j] = (C1.size * C1.P_c[j] + C2.size * C2.P_c[j]) / this.size;
            }
            this.adjComs = new HashMap<>();
            this.adjComs.putAll(C1.adjComs);
            this.adjComs.putAll(C2.adjComs);
            this.adjComs.remove(C1.id);
            this.adjComs.remove(C2.id);
            this.vertices = new HashSet<>();
            this.vertices.addAll(C1.vertices);
            this.vertices.addAll(C2.vertices);
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

        public double modularity() {
            return (internalWeight - (totalWeight * totalWeight / G_total_weight)) / G_total_weight;
        }
    }

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
