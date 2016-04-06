package com.github.st1hy.sabre.dao;

import de.greenrobot.daogenerator.ContentProvider;
import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class SabreDaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1001, "com.github.st1hy.sabre.dao");
        addOpenedImage(schema);

        new DaoGenerator().generateAll(schema, "dao-database/src/main/java");
    }


    private static void addOpenedImage(Schema schema) {
        Entity openedImage = schema.addEntity("OpenedImage");
        openedImage.addIdProperty().index().unique().autoincrement();
        openedImage.addStringProperty("uri").notNull().unique();
        openedImage.addStringProperty("filename");
        openedImage.addDateProperty("date").notNull();

        ContentProvider contentProvider = openedImage.addContentProvider();
        contentProvider.setBasePath("imageHistory");
    }
}
