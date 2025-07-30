package io.sidews.linkerman.base;

import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class BasicSymbolConstructor implements SymbolConstructor {

    private final EPackage ePackage;

    private final ModelDescriptor.Registry descriptorRegistry;

    private final GrammarAnalyzer grammarAnalyzer;

    private final AtomicLong dummyStringCounter = new AtomicLong();

    public BasicSymbolConstructor(ModelDescriptor.Registry registry, EPackage ePackage, GrammarAnalyzer grammarAnalyzer) {
        this.descriptorRegistry = registry;
        this.ePackage = ePackage;
        this.grammarAnalyzer = grammarAnalyzer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EObject> T constructDefaultSymbol(Class<T> symbolType) {
        var descriptor = descriptorRegistry.getOrCreate(symbolType);
        var target = ePackage.getEFactoryInstance().create(
                EMFUtil.findEClassOf(descriptor.getReturnType(), ePackage));

        /*target.eClass().getEAllAttributes().stream()
                .filter(grammarAnalyzer::isFeatureRequired)
                .forEach(attr -> ensureFeatureRequirement(target, attr, this::createAttributeValue));*/
        for (var attr : target.eClass().getEAllAttributes()) {
            grammarAnalyzer.isFeatureRequired(attr);
            ensureFeatureRequirement(target, attr, this::createAttributeValue);
        }


        target.eClass().getEAllContainments().stream()
                .filter(grammarAnalyzer::isFeatureRequired)
                .forEach(ref -> ensureFeatureRequirement(target, ref, this::createReferenceValue));

        return (T) target;
    }

    @SuppressWarnings("unchecked")
    private <T extends EStructuralFeature> void ensureFeatureRequirement(
            EObject target, T feature, Function<T, Object> valueGenerator) {
        if (feature.isMany()) {
            var list = (EList<Object>) target.eGet(feature);
            if (list == null) {
                list = new BasicEList<>();
                target.eSet(feature, list);
            }
            list.add(valueGenerator.apply(feature));
        }
        else if (target.eGet(feature) == null) {
            target.eSet(feature, valueGenerator.apply(feature));
        }
    }

    @SuppressWarnings("unchecked")
    private EObject createReferenceValue(EReference reference) {
        var refType = reference.getEReferenceType();
        var descriptor = descriptorRegistry.getOrCreate((Class<EObject>) refType.getInstanceClass());
        return constructDefaultSymbol(descriptor.getReturnType());
    }

    private Object createAttributeValue(EAttribute attribute) {
        EDataType dataType = attribute.getEAttributeType();
        return switch (dataType.getInstanceClassName()) {
            case "java.lang.String" -> generateDummyString();
            case "boolean", "java.lang.Boolean" -> false;
            case "int", "java.lang.Integer" -> 0;
            case "long", "java.lang.Long" -> 0L;
            case "double", "java.lang.Double" -> 0.0;
            case "float", "java.lang.Float" -> 0.0f;
            default -> dataType.getDefaultValue();
        };
    }

    private String generateDummyString() {
        return "__dummy-" + dummyStringCounter.getAndIncrement();
    }
}


