/**
 * Copyright (C) 2016 - 2021 Norman Kluge
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

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
