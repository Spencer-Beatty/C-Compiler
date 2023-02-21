// Authors: Jonathan Van der Cruysse, Christophe Dubach

// DO NOT MODIFY THIS FILE. For technical grading reasons, we may roll back this file to the original version we
// provided. This will overwrite any and all local changes you made, likely breaking your compiler if you made
// changes.
//
// Open a question on Ed if you need additional features that the classes in this file do not support, such as an
// instruction/opcode that is essential but not currently exposed.

package gen.asm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A MIPS assembly program.
 */
public final class AssemblyProgram {

    /**
     * A section of an assembly program.
     */
    public static final class Section {

        /**
         * An enumeration of section types.
         */
        public enum Type {TEXT, DATA}

        /**
         * The type of this section.
         */
        public final Type type;

        /**
         * Creates a new section.
         * @param type The type of section to create.
         */
        public Section(Type type) {
            this.type = type;
        }

        /**
         * A list of the assembly items in this section.
         */
        public final List<AssemblyItem> items = new ArrayList<>();

        public void emit(Instruction instruction) {
            assert this.type == Type.TEXT;
            items.add(instruction);
        }

        public void emit(OpCode.TernaryArithmetic opcode, Register dst, Register src1, Register src2) {
            emit(new Instruction.TernaryArithmetic(opcode, dst, src1, src2));
        }

        public void emit(OpCode.BinaryArithmetic opcode, Register src1, Register src2) {
            emit(new Instruction.BinaryArithmetic(opcode, src1, src2));
        }

        public void emit(OpCode.UnaryArithmetic opcode, Register dst) {
            emit(new Instruction.UnaryArithmetic(opcode, dst));
        }

        public void emit(OpCode.BinaryBranch opcode, Register src1, Register src2, Label label) {
            emit(new Instruction.BinaryBranch(opcode, src1, src2, label));
        }

        public void emit(OpCode.UnaryBranch opcode, Register src, Label label) {
            emit(new Instruction.UnaryBranch(opcode, src, label));
        }

        public void emit(OpCode.Jump opcode, Label label) {
            emit(new Instruction.Jump(opcode, label));
        }

        public void emit(OpCode.JumpRegister opcode, Register address) {
            emit(new Instruction.JumpRegister(opcode, address));
        }

        public void emit(OpCode.ArithmeticWithImmediate opcode, Register dst, Register src, int imm) {
            emit(new Instruction.ArithmeticWithImmediate(opcode, dst, src, imm));
        }

        public void emit(OpCode.LoadAddress ignoredOpcode, Register dst, Label label) {
            emit(new Instruction.LoadAddress(dst, label));
        }

        public void emit(OpCode.LoadImmediate opcode, Register dst, int immediate) {
            emit(new Instruction.LoadImmediate(opcode, dst, immediate));
        }

        public void emit(OpCode.Load opcode, Register val, Register addr, int imm) {
            emit(new Instruction.Load(opcode, val, addr, imm));
        }

        public void emit(OpCode.Store opcode, Register val, Register addr, int imm) {
            emit(new Instruction.Store(opcode, val, addr, imm));
        }

        public void emit(OpCode.Nullary opcode) {
            emit(Instruction.Nullary.create(opcode));
        }


        public void emit(Label label){
            items.add(label);
        }

        public void emit(Comment comment) {
            items.add(comment);
        }

        public void emit(String comment) {
            items.add(new Comment(comment));
        }

        public void emit(Directive directive) {
            items.add(directive);
        }

        public void print(final PrintWriter writer) {
            switch(type) {
                case DATA : writer.println(".data"); break;
                case TEXT : writer.println(".text"); break;
            }
            items.forEach(item -> {
                    switch(item) {
                        case Comment comment -> writer.println(comment);
                        case Label label -> writer.println(label + ":");
                        case Directive directive -> writer.println(directive);
                        case Instruction instruction -> writer.println(instruction);
                    }}
            );
        }

        @Override
        public String toString()
        {
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            print(pw);
            return sw.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Section section = (Section) o;
            return type == section.type && items.equals(section.items);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, items);
        }
    }


    private Section currSection;

    public final List<Section> sections = new ArrayList<>();

    /**
     * Gets the current section. That is, the section that was last added to this {@link AssemblyProgram}.
     * @return This program's current section.
     */
    public Section getCurrentSection() {
        return currSection;
    }

    /**
     * Appends a given section to this program. The section will become the current section, as produced by
     * {@link #getCurrentSection()}.
     * @param section The section to append to this program.
     */
    public void emitSection(Section section) {
        currSection = section;
        sections.add(currSection);
    }

    /**
     * Creates a new section and appends it to this program. The new section will become the current section, as
     * produced by {@link #getCurrentSection()}.
     * @param type The type of section to create.
     * @return The newly created section.
     */
    public Section newSection(Section.Type type) {
        var section = new Section(type);
        emitSection(section);
        return getCurrentSection();
    }

    /**
     * Sends a textual representation of this assembly program to a {@link PrintWriter}.
     * @param writer The writer to send a textual version of this program to.
     */
    public void print(final PrintWriter writer) {
        sections.forEach(section -> {
            section.print(writer);
            writer.println();
        });

        writer.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssemblyProgram that = (AssemblyProgram) o;
        return sections.equals(that.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sections);
    }
}
