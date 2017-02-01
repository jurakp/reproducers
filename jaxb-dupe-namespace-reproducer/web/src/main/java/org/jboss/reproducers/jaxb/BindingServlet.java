package org.jboss.reproducers.jaxb;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jboss.reproducers.jaxb.schema.MetaData;
import org.jboss.reproducers.jaxb.schema.MetaData.Response;
import org.jboss.reproducers.jaxb.schema.MetaData.Response.IssueDateTime;
import org.jboss.reproducers.jaxb.schema.MetaData.Response.Status;
import org.jboss.reproducers.jaxb.schema.MetaData.Response.Status.EffectiveDateTime;

/**
 * Servlet implementation class BindingServlet. With request parameter validate=true it only validates original XML from the
 * customer with xsd schema. It should fail because of the issue. Invalid content was found starting with element 'Response'.
 * One of '{"urn:se:customs:datamodel:WCO:DocumentMetaData:1":Response}' is expected.
 */
public class BindingServlet extends HttpServlet {
    private static final String USED_JAXB_VERSION = "USED_JAXB_VERSION";
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public BindingServlet() {
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean validate = false;
        if (request.getParameter("validate") != null && Boolean.parseBoolean(request.getParameter("validate"))) {
            validate = true;
        }
        if (validate) {
            URL metaDataSchemaUrl = BindingServlet.class.getClassLoader().getResource("schemas/metadata.xsd");
            Source originalXmlSource = new StreamSource(
                    BindingServlet.class.getClassLoader().getResourceAsStream("metadata.xml"));
            try {
                JAXBContext jaxbCtx = JAXBContext.newInstance("org.jboss.reproducers.jaxb.schema");
                // print version
                System.out.println("Using JAXB version: " + getJaxbVersion(jaxbCtx));
                Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
                Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(metaDataSchemaUrl);
                unmarshaller.setSchema(schema);
                MetaData metaData = unmarshaller.unmarshal(originalXmlSource, MetaData.class).getValue();
                System.out.println("Unmarshalled original XML data. " + metaData);
                response.setHeader(USED_JAXB_VERSION, getJaxbVersion(jaxbCtx));
                response.getWriter().append("Should not work!");
            } catch (Exception e) {
                throw new ServletException("Error validating XML.", e);
            }
        } else {
            try {
                response.setContentType("text/xml");
                JAXBContext jaxbCtx = JAXBContext.newInstance("org.jboss.reproducers.jaxb.schema");
                response.setHeader(USED_JAXB_VERSION, getJaxbVersion(jaxbCtx));
                // print version
                System.out.println("Using JAXB version: " + getJaxbVersion(jaxbCtx));
                Marshaller marshaller = jaxbCtx.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(createTestData(), response.getWriter());
            } catch (Exception e) {
                throw new ServletException("Error marshalling XML.", e);
            }
        }
    }

    private String getJaxbVersion(JAXBContext jaxbCtx) throws Exception {
        Method getBuildId = jaxbCtx.getClass().getDeclaredMethod("getBuildId", new Class[] {});
        return (String) getBuildId.invoke(jaxbCtx);
    }

    private MetaData createTestData() throws DatatypeConfigurationException {
        MetaData metaData = new MetaData();
        metaData.setWCODataModelVersionCode("3.6-SE");
        metaData.setAgencyAssignedCustomizationCode("CWHOP");
        metaData.setAgencyAssignedCustomizationVersionCode(1);
        metaData.setFunctionalDefinition("TLKVT");
        metaData.setResponsibleAgencyName("Swedish Customs");
        metaData.setResponsibleCountryCode("SE");
        Response resp = new Response();
        resp.setFunction(8);
        resp.setTypeCode("ZTL");
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar xmlC = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        IssueDateTime idt = new IssueDateTime();
        idt.setDateTime(xmlC);
        resp.setIssueDateTime(idt);
        Status status = new Status();
        status.setNameCode("TEM");
        EffectiveDateTime etd = new EffectiveDateTime();
        etd.setDateTime(xmlC);
        status.setEffectiveDateTime(etd);
        resp.setStatus(status);
        metaData.setResponse(resp);
        return metaData;
    }
}
