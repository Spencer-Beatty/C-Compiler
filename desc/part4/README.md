# Part IV : Register Allocation


The goal of part IV is to replace the naive register allocator by a proper register allocator. 
As seen in the lecture, you will have to implement:
  1. liveness analysis on a control-flow graph, and
  2. Chaitin's graph colouring algorithm.

You should implement the class `GraphColouringRegAlloc` and you should take inspiration from the naive register allocator.

Once you have made this change, you can run your compiler with your new register allocator using the `-gen colour` option.

> **Marking methodology:** the marking will be done using a series of automated tests.
> The automated tests will check that your register allocator
> * allocates the expected number of architectural registers to virtual registers, and
> * respects program semantics.
>
> We will verify these properties via automated tests.
> Your mark will be a function of the number of tests your compiler compiles correctly with a number of memory accesses no larger than our gold standard (+10%).
>
> Specifically, we will feed assembly programs with virtual registers and `pushRegisters`/`popRegisters`
> pseudo-instructions to your compiler.
> We will check that this assembly program behaves as expected and contains the expected number of memory accesses (within 10% of our gold standard).
> Memory accesses allow us to measure whether your register allocator is spilling excessively.

## 1. Building the Control-Flow Graph (CFG)

Your first task consists of taking the assembly program produced by your code generator (with virtual registers) and transforming each function (there should be one text section per function) into a control flow graph.
Although a production compiler would likely use the concept of basic blocks, we suggest that you keep a simple approach where each node of the CFG contains a single instruction.

For this task, you should write your own directed graph data structure in Java.
In order to verify that you have built the CFG correctly, you may want to write a [Graphviz DOT](https://graphviz.org/doc/info/lang.html) printer to visualize the CFG.

> **Reminder:** You're not allowed to use external libraries.
> All code you submit should be written by you and you alone.
> Do not copy/paste from the web.

## 2. Liveness Analysis

Once you have correctly built the CFG, your next task is to implement liveness analysis.
As seen during the lectures, you should implement this as a simple fixed-point algorithm.
For this project, it is okay to process the nodes in any order (no need to sort them).
However, if you want to ensure a faster convergence time, you should compute the LiveOut set before the LiveIn set as seen in the lecture.

The output of the liveness analysis should be a liveIn and liveOut set for each node in the CFG.

## 3. Interference Graph

Once you have performed liveness analysis, your next step should consist of building the interference graph as seen in the lecture.
The interference graph is an undirected graph and you should implement your own Java data structure to represent it.
A node of the interference graph represents a single virtual register.
Two nodes are connected if there exist a liveIn or LiveOut set where both corresponding virtual registers are present (there is a point in the program execution where both virtual registers are alive at the same time).


## 4. Graph colouring with Chaitin algorithm

Your next job should be to implement Chaitin's algorithm for graph colouring as shown in the lecture.
For this task, your register allocator can only use the following set of MIPS registers when mapping virtual registers: `$t0-9` and `$s0-s7`.
Your allocator should not make any distinctions between the `$s` and `$t` registers (we are deviating from the MIPS convention here and treat all these registers in the same way).
Note that your code generator is of course free to use the other classical MIPS registers such as `$fp` or `$sp`, but the virtual registers should only be mapped to the registers specified above.

> **Hint:** Contrary to what the lecture slides might suggest, you may not want to actually remove nodes from the graph when adding them to the stack as this would complicate your job.
Instead, we suggest that you simply add them to the stack and when you request the list of neighbours from a node, you can simply discard the nodes from that list that appear on the stack.
 

### Spilling

Your allocator should support the ability to spill registers when it is not possible to find a node in the graph with fewer than k neighbours.
As seen in the lectures, there are two approaches you can take.
We suggest that you implement the simple approach which simply reserves a fixed set of registers to be used for spilling.
You should never need more than three registers for this job since this is the maximum number of operands a MIPS instruction can have.
When you need to spill a node, simply remove it from the graph and add it to a set of nodes to be spilled.

## 5. Output produced

Your implementation of the algorithm should produce two results: a set of virtual registers to be spilled (may be empty) and a mapping of virtual registers to architecture registers (`$t0-9`, `$s0-s7`).
Using these two outputs, you should then go back to the list of instructions and patch it up to replace each virtual registers with either an architectural register, or with a load/store instruction if the virtual register is spilled.
You should also at this point expand the two pseudo-instructions `pushRegisters` and `popRegisters`.

You will find it useful to reuse the existing logic from the `NaiveRegAlloc` when performing this step.
As mentioned, you should never need more than three registers when spilling with your register allocator.
Therefore, if you reuse some of the code from the naive reigster allocator, you should really only be using three registers and not six as the original code suggests.


## Debugging Tip: Promote variables to registers

In production compilers, challenging situations for the register allocator typically arise from the use of local variables.
Most local variables are assigned to virtual registers rather than memory by real-life compilers.
Keeping local variables in registers helps reduce memory accesses, but increases register pressure in intricate ways that differ from the register use patterns emerging from temporary values in expressions.

The compiler we used to produce test assembly programs also keeps local variables in registers.
The register use patterns resulting from this variable-to-register promotion are something your register allocator must be able to handle.

You do not need to alter your register allocator for this purpose.
The algorithms from the lectures are sufficiently general.
However, some variable-to-register promotion may trigger bugs in your register allocator that would not have otherwise occurred.

To find these bugs, you may want to update your compiler to store eligible local variables in virtual registers rather than on the stack.
You should do this for every **local** variable, except when:
* the variable is of type struct or array,
* the variable is a function parameter, or
* the variable is used with an addressOf operator.

In all those cases, the allocation should be done on the stack as usual.

We suggest that you implement a pass that runs before code generation starts to identify all the local variables that should be stored in registers.
Then, simply modify your code generator to allocate each identified local variables in a virtual register.

Example:
```c
struct pt_t {
  int x;
  int y;
}

int global_i;    // statically allocated

int foo(int a     /* stack allocated */) {
  int i;          // register allocated
  int j;          // stack allocated (used in addressOf operator)
  int arr[10];    // stack allocated (it is an array)
  struct pt_t pt; // stack allocated (it is a struct)
  int * ptr;      // register allocated
  ...
  ptr = &j;
  ...
}
``` 

> **Important:** Variable-to-register promotion is *optional.* We will not evaluate this part of the assignment. We simply recommend it as a debugging tool.



## New Files

* `gen.regalloc.GraphColouringRegAlloc.java` is the new class you should implement.


## Updated Files

* `Main.java` has been updated to provide a new commandline argument to call the graph colouring register allocator.

