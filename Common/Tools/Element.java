package lab5.Common.Tools;

import lab5.Common.Tools.Validatable;

import java.io.Serializable;

public abstract class Element implements Comparable<Element>, Validatable, Serializable {
    abstract public int getId();
}