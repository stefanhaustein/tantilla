package org.kobjects.asde.lang.type;

public abstract class Instance {
    private final Classifier classifier;

    public Instance(Classifier classifier) {
        this.classifier = classifier;
    }

    public abstract Property getProperty(PropertyDescriptor property);

    public Classifier getClassifier() {
        return classifier;
    }
}
