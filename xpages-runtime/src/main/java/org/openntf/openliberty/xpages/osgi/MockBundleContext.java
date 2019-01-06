package org.openntf.openliberty.xpages.osgi;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class MockBundleContext implements BundleContext {
	
	private final MockBundle bundle;
	
	public MockBundleContext(MockBundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Bundle getBundle() {
		return this.bundle;
	}

	@Override
	public void addBundleListener(BundleListener listener) {

	}

	@Override
	public void addFrameworkListener(FrameworkListener listener) {

	}

	@Override
	public void addServiceListener(ServiceListener listener) {

	}

	@Override
	public void addServiceListener(ServiceListener arg0, String arg1) throws InvalidSyntaxException {

	}

	@Override
	public Filter createFilter(String arg0) throws InvalidSyntaxException {
		return null;
	}

	@Override
	public ServiceReference<?>[] getAllServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
		return null;
	}

	@Override
	public Bundle getBundle(long arg0) {
		return null;
	}

	@Override
	public Bundle getBundle(String arg0) {
		return null;
	}

	@Override
	public Bundle[] getBundles() {
		return null;
	}

	@Override
	public File getDataFile(String arg0) {
		return null;
	}

	@Override
	public String getProperty(String arg0) {
		return null;
	}

	@Override
	public <S> S getService(ServiceReference<S> arg0) {
		return null;
	}

	@Override
	public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> arg0) {
		return null;
	}

	@Override
	public ServiceReference<?> getServiceReference(String arg0) {
		return null;
	}

	@Override
	public <S> ServiceReference<S> getServiceReference(Class<S> arg0) {
		return null;
	}

	@Override
	public ServiceReference<?>[] getServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
		return null;
	}

	@Override
	public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> arg0, String arg1)
			throws InvalidSyntaxException {
		return null;
	}

	@Override
	public Bundle installBundle(String arg0) throws BundleException {
		return null;
	}

	@Override
	public Bundle installBundle(String arg0, InputStream arg1) throws BundleException {
		return null;
	}

	@Override
	public ServiceRegistration<?> registerService(String[] arg0, Object arg1, Dictionary<String, ?> arg2) {
		return null;
	}

	@Override
	public ServiceRegistration<?> registerService(String arg0, Object arg1, Dictionary<String, ?> arg2) {
		return null;
	}

	@Override
	public <S> ServiceRegistration<S> registerService(Class<S> arg0, S arg1, Dictionary<String, ?> arg2) {
		return null;
	}

	@Override
	public <S> ServiceRegistration<S> registerService(Class<S> arg0, ServiceFactory<S> arg1,
			Dictionary<String, ?> arg2) {
		return null;
	}

	@Override
	public void removeBundleListener(BundleListener arg0) {

	}

	@Override
	public void removeFrameworkListener(FrameworkListener arg0) {

	}

	@Override
	public void removeServiceListener(ServiceListener arg0) {

	}

	@Override
	public boolean ungetService(ServiceReference<?> arg0) {
		return false;
	}

}
