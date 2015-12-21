package com.github.st1hy.dao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class SabreDaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1000, "com.github.st1hy.dao");
        addOpenedImage(schema);

        new DaoGenerator().generateAll(schema, "../dao-database/src/main/java");
    }


    private static void addOpenedImage(Schema schema) {
        Entity openedImage = schema.addEntity("OpenedImage");
        openedImage.addIdProperty();
        openedImage.addContentProvider();
        openedImage.addStringProperty("url").notNull();
        openedImage.addDateProperty("date").notNull();
    }
}
