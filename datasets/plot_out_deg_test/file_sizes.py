import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

# --- Define the data ---
# X-axis labels
x_labels = ["Edge List", "Linked List", "Neo4j"]
x_positions = range(len(x_labels))

# Data for the bars (edge sizes are the bottom values, node sizes are the top values)
edge_sizes = [400, 600, 2048]   # Edge List, Linked List, Neo4j respectively
node_sizes = [4, 13, 17]        # Node sizes in kb

# Define colors for the stacks
colors_edge = ["red", "green", "blue"]       # Colors for edge sizes
colors_node = ["darkred", "darkgreen", "darkblue"]  # Colors for node sizes

# --- Plotting ---
plt.figure(figsize=(10, 6))

# Plot the bottom bars (edge sizes)
bars_edge = plt.bar(x_positions, edge_sizes, color=colors_edge, width=0.6, label="Edge Sizes")

# Plot the top bars (node sizes) stacked on top of the edge size bars
bars_node = plt.bar(x_positions, node_sizes, bottom=edge_sizes, color=colors_node, width=0.6, label="Node Sizes")

# Set the x-axis tick labels
plt.xticks(x_positions, x_labels)

# Set labels and title
plt.ylabel("Size (kb)")
plt.title("Stacked Bar Graph: Edge and Node Sizes")

# Create a custom legend (combining both edge and node sizes)
legend_elements = [
    mpatches.Patch(color=colors_edge[0], label="Edge List Edge Size (400 kb)"),
    mpatches.Patch(color=colors_node[0], label="Edge List Node Size (4 kb)"),
    mpatches.Patch(color=colors_edge[1], label="Linked List Edge Size (600 kb)"),
    mpatches.Patch(color=colors_node[1], label="Linked List Node Size (13 kb)"),
    mpatches.Patch(color=colors_edge[2], label="Neo4j Edge Size (2048 kb)"),
    mpatches.Patch(color=colors_node[2], label="Neo4j Node Size (17 kb)")
]
plt.legend(handles=legend_elements, loc='upper left', bbox_to_anchor=(1, 1))

plt.tight_layout()  # Adjust layout to ensure everything fits well

# Display the plot
plt.show()
