package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.KitDef;
import sedonac.namespace.Type;
import sedonac.namespace.TypeUtil;

import static sedonac.translate.TranslationUtil.isCppType;

public abstract class AbstractCppKitTranslator extends AbstractKitTranslator {
    public AbstractCppKitTranslator(Compiler compiler, KitDef kit) {
        super(compiler, kit);
    }

    @Override
    protected boolean acceptType(Type type) {
        return !isCppType(type.qname()) && !type.isEnum() && !TypeUtil.isaTest(type);
    }
}
