/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedona.xml.XException;
import sedonac.CompilerException;
import sedonac.Location;

public class TranslationException extends CompilerException {
    public TranslationException(String msg, AbstractTranslator translator) {
        super(msg, new Location(translator.toFile()));
    }

    public TranslationException(String msg, Location location) {
        super(msg, location);
    }

    public TranslationException(String msg, Location location, Throwable cause) {
        super(msg, location, cause);
    }

    public TranslationException(XException e) {
        super(e);
    }
}
