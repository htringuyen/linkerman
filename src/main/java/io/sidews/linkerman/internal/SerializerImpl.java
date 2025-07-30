package io.sidews.linkerman.internal;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.DynamicModel;
import io.sidews.linkerman.Serializer;
import io.sidews.linkerman.base.DynamicProcessingException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.serializer.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public class SerializerImpl implements Serializer {

    private static final String XMI_EXTENSION = "xmi";

    private final ResourceSet xmiResourceSet;

    private final AtomicLong uriCounter = new AtomicLong(0);

    private final DSLContext dslContext;

    private final ISerializer xtextSerializer;

    public SerializerImpl(DSLContext dslContext) {
        this.xmiResourceSet = new ResourceSetImpl();
        xmiResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(XMI_EXTENSION, new XMIResourceFactoryImpl());
        this.dslContext = dslContext;
        this.xtextSerializer = dslContext.getInjector().getInstance(ISerializer.class);
    }

    @Override
    public String serializeToDSL(DynamicModel<? extends EObject> model) {
        var root = EcoreUtil.getRootContainer(model.getSymbolInstance());
        if (root.eResource() == null) {
            dslContext.getResourceSet().createResource(InMemURIGenerator.createFor(dslContext.getExtension()))
                    .getContents().add(root);
        }
        return xtextSerializer.serialize(root);
    }

    @Override
    public String serializeToXMI(DynamicModel<? extends EObject> model) {
        return serializeToXMI(model.getSymbolInstance());
    }

    @Override
    public String serializeToXMI(EObject symbol) {
        var resource = xmiResourceSet.createResource(InMemURIGenerator.createFor(XMI_EXTENSION));
        resource.getContents().add(symbol);
        var byteStream = new ByteArrayOutputStream();
        try {
            resource.save(byteStream, null);
        }
        catch (IOException e) {
            throw new DynamicProcessingException("Failed to serialize to XMI");
        }
        return byteStream.toString(StandardCharsets.UTF_8);
    }

    @Override
    public EObject deserializeFromXMI(String xmi) {
        var resource = xmiResourceSet.createResource(InMemURIGenerator.createFor(XMI_EXTENSION));
        var byteStream = new ByteArrayInputStream(xmi.getBytes());
        try {
            resource.load(byteStream, null);
            return resource.getContents().getFirst();
        }
        catch (IOException e) {
            throw new DynamicProcessingException("Failed to deserialize XMI");
        }
    }


}
