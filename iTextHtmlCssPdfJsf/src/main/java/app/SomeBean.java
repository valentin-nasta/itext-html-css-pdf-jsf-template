package app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.exceptions.CssResolverException;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

@ManagedBean
public class SomeBean {
	private String someProperty;

	public String getSomeProperty() {
		return (someProperty);
	}

	public void setSomeProperty(String someProperty) {
		this.someProperty = someProperty;
	}

	public String someActionControllerMethod() {
		return ("page-b"); // Means to go to page-b.xhtml (since condition is
							// not mapped in faces-config.xml)
	}

	public String someOtherActionControllerMethod() {
		return ("page-a"); // Means to go to page-a.xhtml (since condition is
							// not mapped in faces-config.xml)
	}

	public String generatePDF() {
		try {
			createPDF();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "asd"; // Means to go to page-a.xhtml (since condition is
						// not mapped in faces-config.xml)
	}

	public void createPDF() throws DocumentException, CssResolverException {
		FacesContext context = FacesContext.getCurrentInstance();

		try {
			String htmlstring = "";// context.getExternalContext()
			// .getRequestParameterMap().get("testForm:htmlstring");

			InputStream htmlpathtest = Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("input/index.html");
			
			htmlstring = CharStreams.toString(new InputStreamReader(htmlpathtest, Charsets.UTF_8));

			InputStream is = new ByteArrayInputStream(htmlstring.getBytes());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// step 1
			Document document = new Document();

			// step 2
			PdfWriter writer = PdfWriter.getInstance(document, baos);

			writer.setInitialLeading(12.5f);

			// step 3
			document.open();

			HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);

			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

			// CSS
			CSSResolver cssResolver = new StyleAttrCSSResolver();
			InputStream csspathtest = Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("input/index.css");
			CssFile cssfiletest = XMLWorkerHelper.getCSS(csspathtest);
			cssResolver.addCss(cssfiletest);

			Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
					new HtmlPipeline(htmlContext, new PdfWriterPipeline(
							document, writer)));

			XMLWorker worker = new XMLWorker(pipeline, true);
			XMLParser p = new XMLParser(worker);
			p.parse(is);//new FileInputStream("results/demo2/walden.html"));

			// step
			document.close();

			// post back...
			HttpServletResponse response = (HttpServletResponse) context
					.getExternalContext().getResponse();
			response.setContentType("application/pdf");
			response.setHeader("Expires", "0");
			response.setHeader("Cache-Control",
					"must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Content-Type", "application/pdf");
			response.setHeader("Content-disposition",
					"attachment;filename=file.pdf");
			response.setContentLength(baos.size());
			OutputStream os = response.getOutputStream();
			baos.writeTo(os);
			os.flush();
			os.close();
			context.responseComplete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
}
