package io.sidews.linkerman.base;

import io.sidews.linkerman.DynamicModel;
import org.eclipse.emf.ecore.EReference;

import java.util.List;

public class LinkStrategies {

    public static DynamicModel.LinkStrategy DEFAULT_SAFEST = List::getFirst;

    public static DynamicModel.LinkStrategy FIRST_MATCHED_CLEAN_ANY = new DynamicModel.LinkStrategy() {
        @Override
        public EReference determineContainingReference(List<EReference> possibleReferences) {
            return possibleReferences.getFirst();
        }

        @Override
        public boolean shouldOverrideSingleFeatures() {
            return true;
        }

        @Override
        public boolean shouldClearCollectionFeatures() {
            return true;
        }
    };
}