

package net.strongdesign.balsa.breezefile.xml;

import java.io.File;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "breezeexpressions")
@XmlAccessorType(XmlAccessType.NONE)
public class BreezeExpressions {

    @XmlElement(name = "component")
    private List<Component> components;

    public static BreezeExpressions readIn(File file) {
        try {
            if(!file.exists()) {
                return null;
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(BreezeExpressions.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            BreezeExpressions retVal = (BreezeExpressions)jaxbUnmarshaller.unmarshal(file);
            return retVal;
        } catch(JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Component> getComponents() {
        return components;
    }
}
