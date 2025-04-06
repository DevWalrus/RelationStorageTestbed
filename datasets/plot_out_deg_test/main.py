import matplotlib.pyplot as plt
import numpy as np

OUT_DEG = r"C:\Users\Clinten\Documents\Courses\2245\Capstone\RelationStorageTestbed\datasets\email-Eu-core.outdeg.full.txt"
TEST_RESULTS = r"C:\Users\Clinten\Documents\Courses\2245\Capstone\RelationStorageTestbed\datasets\email-Eu-core.outdeg.times.txt"

def smooth(data, window_size=10):
    # Simple moving average smoothing using convolution
    window = np.ones(window_size) / window_size
    return np.convolve(data, window, mode='same')

# --- Parse out degree file ---
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

# --- Parse test results file with multiple runs per test ---
# We'll store the runs in a dictionary: for each test name, we store a list of runs.
# Each run is a list of times (one per id).
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
            # When a new test name is encountered, store the previous run (if any)
            if current_test is not None and current_run:
                test_results.setdefault(current_test, []).append(current_run)
                current_run = []
            current_test = line
    # Store the final run
    if current_test is not None and current_run:
        test_results.setdefault(current_test, []).append(current_run)

for test_name in list(test_results.keys()):
    if not "DISK" in test_name:
        filtered_runs = [run for run in test_results[test_name] if max(run) <= 50000]
        if not filtered_runs:
            print(f"Warning: All runs for non _DISK test '{test_name}' were filtered out due to values over 50k.")
        test_results[test_name] = filtered_runs

# --- Average the test times across multiple runs for each test ---
averaged_test_results = {}
for test_name, runs in test_results.items():
    if not runs:
        # Skip tests with no valid runs
        continue
    # If there is more than one run, skip the first run
    if len(runs) > 1:
        runs_to_average = runs[1:]
    else:
        runs_to_average = runs
    # Optionally warn if any run length doesn't match the number of ids
    for run in runs_to_average:
        if len(run) != len(ids):
            print(f"Warning: a run for test '{test_name}' has {len(run)} entries; expected {len(ids)}.")
    runs_array = np.array(runs_to_average)  # shape: (num_runs, num_ids)
    averaged = np.mean(runs_array, axis=0)  # average across runs per id
    averaged_test_results[test_name] = averaged

# Separate tests into two groups: non _DISK and _DISK
non_disk_tests = {}
disk_tests = {}

for test_name, times in averaged_test_results.items():
    if "DISK" in test_name:
        disk_tests[test_name] = times
    else:
        non_disk_tests[test_name] = times

# --- Plotting ---
x = list(range(len(ids)))  # x positions corresponding to each id

# Create two subplots: left for non _DISK tests, right for _DISK tests
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

# For non _DISK tests:
# Create twin axis for test times
ax1_twin = ax1.twinx()

# Plot out degrees on ax1 (left y-axis)
ax1.bar(x, out_degrees, color="lightblue", label="Out Degree")
ax1.set_ylabel("Out Degree")
ax1.set_ylim(bottom=0)
ax1.set_xticks([])

# Plot smoothed averaged test results on ax1_twin (right y-axis)
window_size = 10  # adjust smoothing window size as needed
for test_name, times in non_disk_tests.items():
    if test_name == "EDGE_LIST":
        continue
    smoothed_times = smooth(times, window_size=window_size)
    ax1_twin.plot(x, smoothed_times, label=test_name)
ax1_twin.set_ylabel("Test Times (nanoseconds)")
ax1_twin.set_ylim(bottom=0)
ax1.set_title("Non _DISK Tests")
ax1.set_xlabel("Index (id values not displayed)")

# Combine legends for the non _DISK subplot
lines1, labels1 = ax1.get_legend_handles_labels()
lines1_twin, labels1_twin = ax1_twin.get_legend_handles_labels()
ax1.legend(lines1 + lines1_twin, labels1 + labels1_twin, loc="upper left")

# For _DISK tests:
# Create twin axis for test times
ax2_twin = ax2.twinx()

# Plot out degrees on ax2 (left y-axis)
ax2.bar(x, out_degrees, color="lightblue", label="Out Degree")
ax2.set_ylabel("Out Degree")
ax2.set_ylim(bottom=0)
ax2.set_xticks([])

window_size = 1  # adjust smoothing window size as needed
# Plot smoothed averaged test results on ax2_twin (right y-axis)
for test_name, times in disk_tests.items():
    smoothed_times = smooth(times, window_size=window_size)
    ax2_twin.plot(x, smoothed_times, label=test_name)
ax2_twin.set_ylabel("Test Times (nanoseconds)")
ax2_twin.set_ylim(bottom=0)
ax2.set_title("_DISK Tests")
ax2.set_xlabel("Index (id values not displayed)")

# Combine legends for the _DISK subplot
lines2, labels2 = ax2.get_legend_handles_labels()
lines2_twin, labels2_twin = ax2_twin.get_legend_handles_labels()
ax2.legend(lines2 + lines2_twin, labels2 + labels2_twin, loc="upper left")

fig.suptitle("Out Degree and Smoothed Averaged Test Times")
plt.tight_layout()
plt.show()
