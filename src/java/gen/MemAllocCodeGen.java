package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Directive;
import gen.asm.Label;


import javax.lang.model.element.ModuleElement;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

/* This allocator should deal with all global and local variable declarations. */

public class MemAllocCodeGen extends CodeGen {

    public MemAllocCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    boolean global = true;
    int fpOffset = 0;

    private AssemblyProgram.Section dataSection(){
        try{
            return asmProg.sections.get(0);
        }catch(NullPointerException e){
            throw new IllegalArgumentException("asmProg is null");
        }
    }



    void visit(ASTNode n) {
        switch(n) {
            case null -> {throw new IllegalArgumentException("MemAlloc encountered null Astnode value");}
            case VarDecl vd ->{
                if(global){
                    // create label for it
                    Label label = Label.create(vd.name);
                    dataSection().emit(label);
                    vd.label = label;
                    vd.size = getSize(vd.type);
                    //create size
                    switch(vd.type){
                        case StructType st ->{
                            for(VarDecl var : st.fields){
                                int size = getSize(var.type);
                                dataSection().emit("Field: " + var.name);
                                dataSection().emit(new Directive("space " + size));
                            }

                        }
                        case ArrayType at -> {
                            int size = getSize(at.type);
                            dataSection().emit(new Directive("space " + size));
                        }
                        default -> {
                            int size = getSize(vd.type);
                            dataSection().emit(new Directive("space " + size));
                        }
                    }
                    vd.staticAllocated = true;
                    System.out.println("global");
                }else{
                    // local
                    vd.fpOffset = fpOffset;
                    vd.staticAllocated = false; // false indicates stack allocated
                    vd.size = getSize(vd.type);
                    fpOffset -= vd.size;
                    // this will occur later when the funciton section is created
                    //asmProg.getCurrentSection().emit(OpCode.ADDI, Register.Arch.fp, Register.Arch.fp,-vd.size);
                    System.out.println("local");
                    // put on the stack
                }
            }
            case FunDecl fd ->{
                int offset = 4; // skip return address
                int returnSize = getSize(fd.type);
                fd.returnValFpOffset = offset + returnSize;
                offset += returnSize;
                // feed the parameters in reverse order
                // this will set the first parameter further from the top of the stack
                // which will match it when feeding parameters in later
                List<VarDecl> rev = new ArrayList<VarDecl>();
                rev.addAll(fd.params);
                if(! rev.isEmpty()){
                    Collections.reverse(rev);
                }
                for(VarDecl vd : rev){
                    // parameters are now reversed, here we will take each one
                    // and set its local fp offset for later
                    vd.size = getSize(vd.type);
                    // here we need to set the size fo the vd's
                    vd.fpOffset = offset +  vd.size;
                    offset += vd.size;
                }
                // callSize will be the amount to move the stack pointer down before calling the function;
                //
                fd.callSize = offset;
                this.global = false;
                this.fpOffset = -4; // skips the frame pointer
                // should this be 4 or -4 ?
                // maybe 4 because stores frame pointer at the start?
                System.out.println("fpOffset before entering function: " + fpOffset);
                visit(fd.block);
                this.global = true;
                //frame pointer should be set back to what it was
                System.out.println("fpOffset after leaving function: " + fpOffset);
                // reset frame pointer after function
                fd.declSize = fpOffset; //
            }
            case Block bd ->{
                bd.vds.forEach((innerVarDecl) -> {
                    visit(innerVarDecl);
                });
                bd.stmts.forEach((innerStmt) -> {
                    visit(innerStmt);
                });
            }
            case StructTypeDecl sd -> {
                // todo
                // make unique label with structure name;
                Label structName = Label.get(sd.structType.name);
                sd.label = structName;
                // put label in data section
                AssemblyProgram.Section dataSection = asmProg.sections.get(0);
                dataSection.emit(structName);
                // for each field create a directive of space 4 with size of field
                // then for looking up just go to struct label and increment -4 * field number to get size
                int offset = 0;
                for(VarDecl vd : sd.fields){
                    int size = getSize(vd.type);
                    dataSection.emit("Field: " + vd.name);
                    dataSection.emit(new Directive("word " + offset));
                    offset += size;
                }
            }
            case ASTNode astNode ->{
                astNode.children().forEach((child)->{
                    visit(child);
                });
            }
        }
    }

}
