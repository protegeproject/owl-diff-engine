package org.protege.owl.diff.conf;

import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.align.algorithms.SuperSubClassPinch;
import org.protege.owl.diff.present.algorithms.IdentifyChangedAnnotation;
import org.protege.owl.diff.present.algorithms.IdentifyChangedDefinition;
import org.protege.owl.diff.present.algorithms.IdentifyChangedSuperclass;
import org.protege.owl.diff.present.algorithms.IdentifyRenameOperation;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class DefaultConfiguration extends Configuration {

	public DefaultConfiguration() {
		addAlignmentAlgorithm(MatchByCode.class);
		addAlignmentAlgorithm(MatchById.class);
		addAlignmentAlgorithm(MatchStandardVocabulary.class);
		addAlignmentAlgorithm(SuperSubClassPinch.class);
		
		addPresentationAlgorithm(IdentifyChangedAnnotation.class);
		addPresentationAlgorithm(IdentifyChangedDefinition.class);
		addPresentationAlgorithm(IdentifyChangedSuperclass.class);
		addPresentationAlgorithm(IdentifyRenameOperation.class);
		
		put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, OWLRDFVocabulary.RDFS_LABEL.getIRI().toString());
	}
}
