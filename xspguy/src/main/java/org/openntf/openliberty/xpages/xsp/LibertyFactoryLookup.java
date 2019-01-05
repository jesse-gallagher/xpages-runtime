package org.openntf.openliberty.xpages.xsp;

import com.ibm.xsp.binding.id.ClientIdBindingFactory;
import com.ibm.xsp.binding.javascript.JavaScriptBindingFactoryImpl;
import com.ibm.xsp.binding.xpath.XPathBindingFactoryImpl;
import com.ibm.xsp.designer.context.ServletXSPContextFactory;
import com.ibm.xsp.factory.FactoryLookup;
import com.ibm.xsp.javascript.JavaScriptFactoryImpl;
import com.ibm.xsp.model.ExtsnDataModelFactory;

import java.util.Iterator;

public class LibertyFactoryLookup extends FactoryLookup {
    private final FactoryLookup delegate;
    
    public LibertyFactoryLookup(FactoryLookup delegate) {
        this.delegate = delegate;

        setFactory("com.ibm.xsp.XSP_CONTEXT_FACTORY", new ServletXSPContextFactory());
        setFactory("com.ibm.xsp.EXTSN_DATAMODEL_FACTORY", new ExtsnDataModelFactory());
        setFactory("com.ibm.xsp.JAVASCRIPT_FACTORY", new JavaScriptFactoryImpl());
        setFactory("javascript", new JavaScriptBindingFactoryImpl());
        setFactory("xpath", new XPathBindingFactoryImpl());
        setFactory("id", new ClientIdBindingFactory());
    }

    @Override
    public Iterator getFactories() {
        System.out.println("Asked for getFactories, which is " + super.getFactories());
        return super.getFactories();
    }

    @Override
    public Iterator getFactories(Class aClass) {
        System.out.println("Asked for factories for " + aClass);
        return super.getFactories(aClass);
    }

    @Override
    public Object getFactory(String s) {
        System.out.println("Asked for the factory for " + s + ", which is " + super.getFactory(s));
        return super.getFactory(s);
    }
}
