/*
 * Generated file. 
 * 
 * LibertyTest.java
 */
package xsp;

import com.ibm.xsp.page.compiled.AbstractCompiledPage;
import com.ibm.xsp.page.compiled.AbstractCompiledPageDispatcher;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.ibm.xsp.page.compiled.NoSuchComponentException;
import com.ibm.xsp.page.compiled.PageExpressionEvaluator;
import java.lang.String;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.component.UIPassThroughTag;
import com.ibm.xsp.component.xp.XspOutputText;
import java.lang.Object;
import javax.faces.el.ValueBinding;
import com.ibm.xsp.component.xp.XspDiv;
import com.ibm.xsp.component.xp.XspCommandButton;
import com.ibm.xsp.component.xp.XspEventHandler;

@SuppressWarnings("all")
public class LibertyTest extends AbstractCompiledPageDispatcher{
    
    public LibertyTest() {
        super("3.0");
    }

    protected AbstractCompiledPage createPage(int pageIndex) {
        return new LibertyTestPage();
    }
    
    public static class LibertyTestPage extends AbstractCompiledPage {
        
        private static final ComponentInfo[] s_infos = new ComponentInfo[]{
            ComponentInfo.EMPTY_NORMAL, // 0 text
            new ComponentInfo(true, new int[]{0}), // 1 p
            ComponentInfo.EMPTY_NORMAL, // 2 text2
            ComponentInfo.EMPTY_NORMAL, // 3 text3
            new ComponentInfo(false, new int[]{2, 3}), // 4 "refresher"
            ComponentInfo.EMPTY_NORMAL, // 5 eventHandler
            new ComponentInfo(false, new int[]{5}), // 6 "clickme"
            new ComponentInfo(false, new int[]{1, 4, 6}), // 7 view
        };
        
        public LibertyTestPage() {
            super(7, s_infos );
        }
        
        public int getComponentForId(String id) throws NoSuchComponentException { 
            if( "refresher".equals(id) )
                return 4;
            if( "clickme".equals(id) )
                return 6;
            return -1;
        }
        
        public UIComponent createComponent(int id, FacesContext context,
                UIComponent parent, PageExpressionEvaluator evaluator)
                throws NoSuchComponentException { 
            switch (id) {
            case 7:
                return createView(context, parent, evaluator);
            case 1:
                return createP(context, parent, evaluator);
            case 0:
                return createText(context, parent, evaluator);
            case 4:
                return createRefresher(context, parent, evaluator);
            case 2:
                return createText2(context, parent, evaluator);
            case 3:
                return createText3(context, parent, evaluator);
            case 6:
                return createClickme(context, parent, evaluator);
            case 5:
                return createEventHandler(context, parent, evaluator);
            }
            throw new NoSuchComponentException(id);
        }
        
        private UIComponent createView(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            UIViewRootEx2 result = new UIViewRootEx2();
            initViewRoot(result);
            return result;
        }

        private UIComponent createP(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            UIPassThroughTag component = new UIPassThroughTag();
            component.setTag("p");
            return component;
        }

        private UIComponent createText(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            XspOutputText result = new XspOutputText();
            String sourceId = "/xp:view[1]/p[1]/xp:text[1]/@value";
            String valueExpr = "I\'m an XPage, running on #{facesContext.externalContext.request.session.servletContext.serverInfo}, which implements Servlet #{facesContext.externalContext.request.session.servletContext.majorVersion}.#{facesContext.externalContext.request.session.servletContext.minorVersion}";
            ValueBinding value = evaluator.createValueBinding(result, valueExpr, sourceId,Object.class);
            result.setValueBinding("value", value);
            return result;
        }

        private UIComponent createRefresher(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            XspDiv result = new XspDiv();
            setId(result, "refresher");
            return result;
        }

        private UIComponent createText2(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            XspOutputText result = new XspOutputText();
            String sourceId = "refresher/xp:text[1]/@value";
            String valueExpr = "#{javascript:facesContext.getApplication().getFactoryLookup().getFactory(\'javascript\')}";
            ValueBinding value = evaluator.createValueBinding(result, valueExpr, sourceId,Object.class);
            result.setValueBinding("value", value);
            return result;
        }

        private UIComponent createText3(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            XspOutputText result = new XspOutputText();
            String sourceId = "refresher/xp:text[2]/@value";
            String valueExpr = "#{javascript:new Date().getTime()}";
            ValueBinding value = evaluator.createValueBinding(result, valueExpr, sourceId,Object.class);
            result.setValueBinding("value", value);
            return result;
        }

        private UIComponent createClickme(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            XspCommandButton result = new XspCommandButton();
            setId(result, "clickme");
            result.setValue("Click me");
            return result;
        }

        private UIComponent createEventHandler(FacesContext context, 
                UIComponent parent, PageExpressionEvaluator evaluator) {
            XspEventHandler result = new XspEventHandler();
            result.setEvent("onclick");
            result.setRefreshId("refresher");
            result.setRefreshMode("partial");
            result.setSubmit(true);
            return result;
        }

    }
}
