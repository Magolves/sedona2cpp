package sedonac.translate;

import sedona.util.TextUtil;
import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.CompilerSupport;
import sedonac.ast.KitDef;
import sedonac.ast.TypeDef;
import sedonac.namespace.Slot;
import sedonac.namespace.StubType;
import sedonac.namespace.Type;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static sedonac.translate.AbstractTranslator.SPACES_PER_INDENT;

/**
 * Base class for all kit translators. This class is intended to generate files which
 * are on 'kit' scopde like build files.
 */
public abstract class AbstractKitTranslator extends CompilerSupport {
    public KitDef kit;
    public File outDir;
    public PrintWriter out;
    public int indent;

    public AbstractKitTranslator(Compiler compiler, KitDef kit) {
        super(compiler);
        this.kit = kit;
        this.outDir = compiler.translation.outDir;
    }

    //region Translation methods
    public void translate()
            throws IOException {
        File file = toFile();
        log.debug("    Translate [" + file + "]");
        out = new PrintWriter(new FileWriter(file));
        try {
            header(getHeaderText());
            doTranslate();
            log.debug("    Wrote [" + file.getAbsolutePath() + "]");
        } finally {
            out.close();
        }
    }

    /**
     * Checks if given type should be translated or not. This method returns always true
     * @param type the type to check
     * @return true, if given type shall be translated; otherwise false.
     */
    protected boolean acceptType(Type type) {
        return true;
    }

    public void header(String purpose) {
        w("/*********************************************************").nl();
        w(" * " + purpose).nl();
        w(" * (C) Robert Bosch GmbH 2019").nl();
        w(" * Tag      : $Id$").nl();
        w(" * Namespace: " + kit.name).nl();
        w(" * Generated: " + new Date()).nl();
        w(" *********************************************************/").nl().nl();
    }

    //region Output
    public AbstractKitTranslator w(Object s) {
        out.print(s);
        return this;
    }

    public AbstractKitTranslator w(int i) {
        out.print(i);
        return this;
    }

    public AbstractKitTranslator indent() {
        out.print(TextUtil.getSpaces(indent * SPACES_PER_INDENT));
        return this;
    }

    public AbstractKitTranslator nl() {
        out.println();
        return this;
    }

    public AbstractKitTranslator wtype(Type t) {
        out.print(toType(t));
        return this;
    }
    //endregion

    //region Type utils
    public String qname(Type type) {
        return type.kit().name() + "_" + type.name();
    }

    public String qname(Slot s) {
        return qname(s.parent()) + "_" + s.name();
    }


    public String toType(Type type) {
        return toType(type, false);
    }

    public String toType(Type type, boolean inline) {
        return toInlineType(type);
    }

    public String toInlineType(Type type) {
        if (type.isArray()) {
            return toType(type.arrayOf());
        } else if (type.isPrimitive()) {
            if (type.isBool()) return "bool";
            if (type.isByte()) return "uint8_t";
            if (type.isShort()) return "uint16_t";
            if (type.isInt()) return "int32_t";
            return type.signature();
        } else if (type instanceof StubType) {
            // shouldn't need this once I wrap up
            // bootstrap type resolve
            return "/*STUB!*/sys::" + type.name();
        } else {
            return qname(type);
        }
    }
    //endregion

    /**
     * Gets the output file
     * @return the output file
     */
    public abstract File toFile();

    /**
     * Generates the file content.
     */
    public abstract void doTranslate();

    /**
     * Gets the header message (file description)
     * @return
     */
    protected abstract String getHeaderText();
}
