package org.kobjects.typesystem;

public abstract class Instance implements Typed {
    private final Classifier classifier;

    public Instance(Classifier classifier) {
        this.classifier = classifier;
    }

    public abstract Property getProperty(PropertyDescriptor property);

/*    public Classifier getClassifier() {
        return classifier;
    }*/

    public Classifier getType() { return classifier; }
}
