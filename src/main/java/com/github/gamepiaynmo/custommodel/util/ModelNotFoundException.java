package com.github.gamepiaynmo.custommodel.util;

import java.io.FileNotFoundException;

public class ModelNotFoundException extends TranslatableException {
    public ModelNotFoundException(String fileName) {
        super("error.custommodel.loadmodelpack.notfound", fileName);
    }
}
