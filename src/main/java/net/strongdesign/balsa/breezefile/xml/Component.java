

package net.strongdesign.balsa.breezefile.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class Component {
    @XmlAttribute(name = "name")
    private String name;
    @XmlValue
    private String expression;

    public String getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }
}
