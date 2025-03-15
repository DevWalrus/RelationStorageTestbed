package Algos;

import java.util.*;
import Graphs.IGraph;

public class Walktrap<T> {
    private final IGraph<T> graph;
    private final int t; // random walk length
    private final int desiredCommunities; // stopping criterion: desired number of communities

    // Constructor: supply the graph, the random walk length, and the desired number of communities.
    public Walktrap(IGraph<T> graph, int t, int desiredCommunities) {
        this.graph = graph;
        this.t = t;
        this.desiredCommunities = desiredCommunities;
    }

    // Compute the probability distribution for a random walk of length t starting from a given node.
    // The distribution is represented as a map from node -> probability.
    private Map<T, Double> computeRandomWalkDistribution(T start) {
        Map<T, Double> prob = new HashMap<>();
        prob.put(start, 1.0);
        for (int step = 0; step < t; step++) {
            Map<T, Double> nextProb = new HashMap<>();
            // For each node in the current distribution, distribute its probability equally among neighbors.
            for (Map.Entry<T, Double> entry : prob.entrySet()) {
                T current = entry.getKey();
                double currentProb = entry.getValue();
                List<T> neighbors = new ArrayList<>();
                for (T nb : graph.getNeighbors(current)) {
                    neighbors.add(nb);
                }
                int degree = neighbors.size();
                if (degree == 0) continue; // dead end
                double share = currentProb / degree;
                for (T nb : neighbors) {
                    nextProb.put(nb, nextProb.getOrDefault(nb, 0.0) + share);
                }
            }
            prob = nextProb;
        }
        return prob;
    }

    // A simple Euclidean distance between two probability distributions.
    // More refined approaches might weight by node degrees.
    private double distance(Map<T, Double> p1, Map<T, Double> p2) {
        Set<T> keys = new HashSet<>();
        keys.addAll(p1.keySet());
        keys.addAll(p2.keySet());
        double sumSq = 0.0;
        for (T key : keys) {
            double v1 = p1.getOrDefault(key, 0.0);
            double v2 = p2.getOrDefault(key, 0.0);
            sumSq += Math.pow(v1 - v2, 2);
        }
        return Math.sqrt(sumSq);
    }

    // Main method that performs community detection using an agglomerative strategy.
    // Returns a mapping from each node to its final community ID.
    public Map<T, Integer> detectCommunities() {
        // Initialize: each node is its own community.
        Map<T, Integer> communityAssignment = new HashMap<>();
        int commId = 0;
        for (T node : graph.getNodes()) {
            communityAssignment.put(node, commId++);
        }

        // For each initial community (a single node), compute the random walk distribution.
        Map<Integer, Map<T, Double>> communityDistributions = new HashMap<>();
        for (T node : graph.getNodes()) {
            int cid = communityAssignment.get(node);
            communityDistributions.put(cid, computeRandomWalkDistribution(node));
        }

        // Build initial distances between all pairs of communities.
        PriorityQueue<MergeCandidate> mergeQueue = new PriorityQueue<>();
        List<Integer> commIds = new ArrayList<>(communityDistributions.keySet());
        for (int i = 0; i < commIds.size(); i++) {
            for (int j = i + 1; j < commIds.size(); j++) {
                int id1 = commIds.get(i);
                int id2 = commIds.get(j);
                double d = distance(communityDistributions.get(id1), communityDistributions.get(id2));
                mergeQueue.add(new MergeCandidate(id1, id2, d));
            }
        }

        // Agglomerative clustering:
        // Merge the two communities with the smallest distance until we reach the desired number of communities.
        while (communityDistributions.size() > desiredCommunities) {
            MergeCandidate candidate = mergeQueue.poll();
            if (candidate == null) break;
            int c1 = candidate.comm1;
            int c2 = candidate.comm2;
            if (!communityDistributions.containsKey(c1) || !communityDistributions.containsKey(c2))
                continue;

            // Merge c1 and c2 into a new community (reuse the lower ID).
            int newCommId = Math.min(c1, c2);
            Map<T, Double> dist1 = communityDistributions.get(c1);
            Map<T, Double> dist2 = communityDistributions.get(c2);
            Map<T, Double> newDist = new HashMap<>();
            // For simplicity, assume equal weighting; a more accurate version would weight by community size.
            for (T key : dist1.keySet()) {
                newDist.put(key, dist1.get(key));
            }
            for (T key : dist2.keySet()) {
                newDist.put(key, newDist.getOrDefault(key, 0.0) + dist2.get(key));
            }
            for (T key : newDist.keySet()) {
                newDist.put(key, newDist.get(key) / 2.0);
            }

            // Update communities: remove old communities and add the merged one.
            communityDistributions.remove(c1);
            communityDistributions.remove(c2);
            communityDistributions.put(newCommId, newDist);
            // Update the community assignments.
            for (Map.Entry<T, Integer> entry : communityAssignment.entrySet()) {
                if (entry.getValue() == c1 || entry.getValue() == c2) {
                    entry.setValue(newCommId);
                }
            }
            // Update the merge queue: compute distances between the new community and all others.
            for (Integer other : communityDistributions.keySet()) {
                if (other == newCommId) continue;
                double d = distance(communityDistributions.get(newCommId), communityDistributions.get(other));
                mergeQueue.add(new MergeCandidate(newCommId, other, d));
            }
        }

        return communityAssignment;
    }

    // Helper class for storing merge candidates (pairs of communities with their computed distance).
    private static class MergeCandidate implements Comparable<MergeCandidate> {
        int comm1;
        int comm2;
        double distance;

        public MergeCandidate(int comm1, int comm2, double distance) {
            this.comm1 = comm1;
            this.comm2 = comm2;
            this.distance = distance;
        }

        @Override
        public int compareTo(MergeCandidate other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}
