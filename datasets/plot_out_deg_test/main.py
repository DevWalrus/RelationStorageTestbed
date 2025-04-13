import matplotlib.pyplot as plt
import numpy as np
import os
import sys
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("test_name", nargs="?", default="com-dblp.ungraph", help="Test name")
parser.add_argument("-p", "--poster", action="store_true", help="Enable poster mode: large font size and no legends")
args = parser.parse_args()
test_name = args.test_name
poster = args.poster

if poster:
    plt.rcParams.update({'font.size': 30})  # Large font size for poster mode
else:
    plt.rcParams.update({'font.size': 20})  # Normal font size

def generate_file_paths(test_name):
    """
    Generates file paths for the out-degree and test results files based on the given test name.
    
    Parameters:
        test_name (str): The identifier for the test (e.g., "com-dblp.ungraph").
        
    Returns:
        tuple: (out_deg_path, test_results_path)
    """
    base_dir = r"C:\Users\Clinten\Documents\Courses\2245\Capstone\RelationStorageTestbed\datasets"
    out_deg_path = os.path.join(base_dir, f"{test_name}.outdeg.full.txt")
    test_results_path = os.path.join(base_dir, f"{test_name}.outdeg.times.txt")
    return out_deg_path, test_results_path

# Generate file paths using the provided test name
OUT_DEG, TEST_RESULTS = generate_file_paths(test_name)

OUTPUT_SUFFIX = '.big.png' if poster else '.png'

def smooth(data, window_size=10):
    """Simple moving average smoothing using convolution."""
    window = np.ones(window_size) / window_size
    return np.convolve(data, window, mode='same')

# -------------------------
# Data Parsing (unchanged)
# -------------------------
ids = []
out_degrees = []

with open(OUT_DEG, "r") as f:
    for line in f:
        line = line.strip()
        if not line or ":" not in line:
            continue
        id_part, degree_part = line.split(":", 1)
        ids.append(id_part.strip())
        try:
            out_degrees.append(int(degree_part.strip()))
        except ValueError:
            print(f"Warning: could not convert {degree_part.strip()} to integer.")

# Parse test results file with multiple runs per test
test_results = {}
current_test = None
current_run = []

with open(TEST_RESULTS, "r") as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        if line.isdigit():
            if current_test is None:
                print("Error: numeric value encountered before any test name.")
            else:
                current_run.append(int(line))
        else:
            if current_test is not None and current_run:
                test_results.setdefault(current_test, []).append(current_run)
                current_run = []
            current_test = line
    if current_test is not None and current_run:
        test_results.setdefault(current_test, []).append(current_run)

# Filter and average test results as before
for test_name_key in list(test_results.keys()):
    if not test_name_key.endswith('_'):
        filtered_runs = [run for run in test_results[test_name_key] if max(run) <= 50000]
        if not filtered_runs:
            print(f"Warning: All runs for non _DISK test '{test_name_key}' were filtered out due to values over 50k.")
        test_results[test_name_key] = filtered_runs

averaged_test_results = {}
for test_name_key, runs in test_results.items():
    if not runs:
        continue
    # If more than one run exists, skip the first one
    runs_to_average = runs[1:] if len(runs) > 1 else runs
    for run in runs_to_average:
        if len(run) != len(ids):
            print(f"Warning: a run for test '{test_name_key}' has {len(run)} entries; expected {len(ids)}.")
    runs_array = np.array(runs_to_average)
    averaged = np.mean(runs_array, axis=0)
    averaged_test_results[test_name_key] = averaged

# Separate tests into two groups: non _DISK and _DISK
non_disk_tests = {}
disk_tests = {}

for test_name_key, times in averaged_test_results.items():
    if test_name_key.endswith('_'):
        disk_tests[test_name_key[:-1]] = times
    else:
        non_disk_tests[test_name_key] = times

x = list(range(len(ids)))  # x positions corresponding to each id

# -------------------------
# Define Color Mapping for Test Series
# -------------------------
color_map = {
    "Neo4j": "blue",
    "Adjacency Matrix": "orange",
    "Edge List": "red",
    "Adjacency List": "green"
}
default_color = "black"  # default color if test name doesn't match

# ----------------------
# Figure 1: Non _DISK Tests (Normal Scale)
# ----------------------
fig1, ax1 = plt.subplots(figsize=(12, 8))
ax1_twin = ax1.twinx()

# Plot out degrees on left y-axis
ax1.bar(x, out_degrees, color="lightblue", label="Out Degree")
ax1.set_ylabel("Out Degree")
ax1.set_ylim(bottom=0)
ax1.set_xticks([])

# Plot smoothed averaged test results on right y-axis using the color mapping
window_size = 10  # adjust smoothing window size as needed
for test_name_key, times in non_disk_tests.items():
    smoothed_times = smooth(times, window_size=window_size)
    color = color_map.get(test_name_key, default_color)
    if not poster:
        ax1_twin.plot(x, smoothed_times, color=color, label=test_name_key)
    else:
        ax1_twin.plot(x, smoothed_times, color=color)
if not poster:
    handles1, labels1 = ax1.get_legend_handles_labels()
    handles2, labels2 = ax1_twin.get_legend_handles_labels()
    ax1.legend(handles1 + handles2, labels1 + labels2)
ax1_twin.set_ylabel("Test Times (nanoseconds)")
ax1_twin.set_ylim(bottom=0)

fig1.savefig("non_disk_tests" + OUTPUT_SUFFIX, dpi=900, bbox_inches='tight', pad_inches=0)

# ----------------------
# Figure 2: _DISK Tests (Log Scale)
# ----------------------
fig2, ax2 = plt.subplots(figsize=(12, 8))
ax2_twin = ax2.twinx()

# Plot out degrees on left y-axis
ax2.bar(x, out_degrees, color="lightblue", label="Out Degree")
ax2.set_ylabel("Out Degree")
ax2.set_ylim(bottom=0)
ax2.set_xticks([])

# Plot smoothed averaged test results (log scale) using the same color mapping
window_size = 1  # adjust smoothing window size as needed for _DISK tests
for test_name_key, times in disk_tests.items():
    smoothed_times = smooth(times, window_size=window_size)
    color = color_map.get(test_name_key, default_color)
    if not poster:
        ax2_twin.plot(x, smoothed_times, color=color, label=test_name_key)
    else:
        ax2_twin.plot(x, smoothed_times, color=color)
if not poster:
    handles1, labels1 = ax2.get_legend_handles_labels()
    handles2, labels2 = ax2_twin.get_legend_handles_labels()
    ax2.legend(handles1 + handles2, labels1 + labels2)
ax2_twin.set_ylabel("Test Times (nanoseconds)")
ax2_twin.set_yscale("log")
ax2_twin.set_ylim(bottom=50)

fig2.savefig("disk_tests" + OUTPUT_SUFFIX, dpi=900, bbox_inches='tight', pad_inches=0)

# Finally, display both figures (if desired)
plt.show()
