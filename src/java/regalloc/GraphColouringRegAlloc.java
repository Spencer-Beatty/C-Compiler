package regalloc;

import gen.asm.AssemblyPass;
import gen.asm.AssemblyProgram;

public class GraphColouringRegAlloc implements AssemblyPass {

    public static final GraphColouringRegAlloc INSTANCE = new GraphColouringRegAlloc();


    @Override
    public AssemblyProgram apply(AssemblyProgram program) {

        AssemblyProgram newProg = new AssemblyProgram();
        ControlFlowGraph cg = new ControlFlowGraph();
        cg.CreateControlFlowGraph(program);
        cg.Display();

        // To complete

        return newProg;
    }



}
