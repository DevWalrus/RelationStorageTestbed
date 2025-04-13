import sys
import struct

# Updated constants based on the new node layout.
NODE_RECORD_SIZE = 17   # 1 (bool) + 8 (outgoing pointer) + 8 (incoming pointer)
REL_RECORD_SIZE = 41    # remains the same for relationship records

def read_nodes(node_filename):
    """Reads the node file and returns the node count and a list of node records."""
    with open(node_filename, "rb") as nf:
        # Read the first 4 bytes for the count (big-endian unsigned int)
        count_bytes = nf.read(4)
        if len(count_bytes) < 4:
            raise ValueError("Node file does not contain enough bytes for node count")
        node_count = struct.unpack(">I", count_bytes)[0]

        nodes = []
        for i in range(node_count):
            rec = nf.read(NODE_RECORD_SIZE)
            if len(rec) != NODE_RECORD_SIZE:
                raise ValueError(f"Node record {i} is incomplete")
            # Use format ">Bqq": 
            # B: 1 byte for the boolean (in use)
            # q: 8-byte signed long for the outgoing pointer
            # q: 8-byte signed long for the incoming pointer.
            in_use, out_ptr, in_ptr = struct.unpack(">Bqq", rec)
            nodes.append({
                "in_use": in_use,
                "first_rel": out_ptr,  # use the outgoing pointer for relationship chaining
                "incoming_ptr": in_ptr  # available if needed
            })
    return node_count, nodes

def read_relationship_at(rel_file, pointer):
    """Given an open edge file and a pointer, read one relationship record."""
    rel_file.seek(pointer)
    rec = rel_file.read(REL_RECORD_SIZE)
    if len(rec) != REL_RECORD_SIZE:
        raise ValueError(f"Relationship record at fp {pointer} is incomplete")
    # Format: two ints, four longs, one bool. ("q" for 8-byte signed long)
    start_node, end_node, start_next, start_prev, end_next, end_prev, end_chain_flag = struct.unpack(">iiqqqqB", rec)
    return {
        "start_node": start_node,
        "end_node": end_node,
        "start_next": start_next,
        "start_prev": start_prev,
        "end_next": end_next,
        "end_prev": end_prev,
        "end_chain_flag": end_chain_flag
    }

def process_graph(node_filename, rel_filename):
    # Read nodes from the node file
    node_count, nodes = read_nodes(node_filename)
    print(f"There are {node_count} nodes")

    with open(rel_filename, "rb") as rf:
        # Read the relationship count from the first 4 bytes
        rel_count_bytes = rf.read(4)
        if len(rel_count_bytes) < 4:
            raise ValueError("Edge file does not contain enough bytes for edge count")
        rel_count = struct.unpack(">I", rel_count_bytes)[0]
        # Optionally, print the edge count:
        # print(f"There are {rel_count} edges")

        # Process each node
        for idx, node in enumerate(nodes):
            if node["in_use"] == 0:
                print(f"\tIndex {idx}: Not in use")
            else:
                out_line = f"\tIndex {idx}: In use, Relationships start at fp {node['first_rel']}"
                # With the new design, an empty pointer is -1, not 0.
                if node["first_rel"] == -1:
                    print(out_line + ", no relationships.")
                else:
                    print(out_line + ",")
                    pointer = node["first_rel"]
                    chain = []
                    while pointer != -1:
                        rel = read_relationship_at(rf, pointer)
                        chain.append((pointer, rel))
                        # If the end-of-chain flag (last field) is true (nonzero), break out.
                        if rel["end_chain_flag"]:
                            break
                        pointer = rel["start_next"]

                    # Build the output for the chain.
                    for i, (ptr, rec) in enumerate(chain):
                        if i == 0:
                            print(f"\t\tThe neighbor is {rec['end_node']}", end="")
                        else:
                            print(f", the next neighbor is at fp {ptr}, the neighbor is {rec['end_node']}", end="")
                    print(", there is no next neighbor.")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python graph_info.py <node_file> <edge_file>")
        sys.exit(1)
    node_file = sys.argv[1]
    rel_file = sys.argv[2]
    process_graph(node_file, rel_file)
