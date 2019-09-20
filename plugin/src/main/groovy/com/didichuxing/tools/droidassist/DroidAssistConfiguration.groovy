package com.didichuxing.tools.droidassist

import com.didichuxing.tools.droidassist.transform.SourceTargetTransformer
import com.didichuxing.tools.droidassist.transform.Transformer
import com.didichuxing.tools.droidassist.transform.around.*
import com.didichuxing.tools.droidassist.transform.enhance.*
import com.didichuxing.tools.droidassist.transform.insert.*
import com.didichuxing.tools.droidassist.transform.replace.*
import org.gradle.api.Project

/**
 * Entity class for config file where is from droidAssistOptions
 */
class DroidAssistConfiguration {

    Project project

    def globalIncludes = []
    def globalExcludes = []
    def transformers = new ArrayList<Transformer>()

    def METHOD = "METHOD"
    def CONSTRUCTOR = "CONSTRUCTOR"
    def FIELD = "FIELD"
    def INITIALIZER = "INITIALIZER"

    def sourceTargetTransformerNodeHandler = {
        kind, node, transformerFeather ->
            SourceTargetTransformer transformer = transformerFeather.call()
            def extend = node.Source.@extend[0] ?: "true"
            transformer.setSource(node.Source.text().trim(), kind, Boolean.valueOf(extend))
            transformer.setTarget(node.Target.text().trim())

            if (transformer.getSource() == '') {
                throw new IllegalArgumentException("Empty source in node ${node}")
            }
            if (transformer.getTarget() == '') {
                throw new IllegalArgumentException("Empty target in node ${node}")
            }

            def includes = [], excludes = []

            node.Filter.Include.each { includes.add(it.text()) }
            node.Filter.Exclude.each { excludes.add(it.text()) }

            if (!Boolean.valueOf(node.Filter.@ignoreGlobalIncludes[0])) {
                includes.addAll(globalIncludes)
            }
            if (!Boolean.valueOf(node.Filter.@ignoreGlobalExcludes[0])) {
                excludes.addAll(globalExcludes)
            }

            transformer.classFilterSpec.addIncludes(includes)
            transformer.classFilterSpec.addExcludes(excludes)
            transformers.add(transformer)
    }

    def aroundTransformerNodeHandler = {
        kind, node, transformerFeather ->
            AroundTransformer transformer = transformerFeather.call()
            def extend = node.Source.@extend[0] ?: "true"
            transformer.setSource(node.Source.text().trim(), kind, Boolean.parseBoolean(extend))
            transformer.setTargetBefore(node.TargetBefore.text().trim())
            transformer.setTargetAfter(node.TargetAfter.text().trim())

            if (transformer.getSource() == '') {
                throw new IllegalArgumentException("Empty source in node ${node}")
            }
            if (transformer.getTargetBefore() == '') {
                throw new IllegalArgumentException("Empty target before in node ${node}")
            }
            if (transformer.getTargetAfter() == '') {
                throw new IllegalArgumentException("Empty target after in node ${node}")
            }

            def includes = [], excludes = []

            node.Filter.Include.each { includes.add(it.text()) }
            node.Filter.Exclude.each { excludes.add(it.text()) }

            includes.addAll(globalIncludes)
            excludes.addAll(globalExcludes)

            transformer.classFilterSpec.addIncludes(includes)
            transformer.classFilterSpec.addExcludes(excludes)
            transformers.add(transformer)
    }

    def addCatchTransformerNodeHandler = {
        kind, node, transformerFeather ->
            TryCatchTransformer transformer = transformerFeather.call()
            def extend = node.Source.@extend[0] ?: "true"
            transformer.setSource(node.Source.text().trim(), kind, Boolean.parseBoolean(extend))
            transformer.setException(node.Exception.text().trim())
            transformer.setTarget(node.Target.text().trim())

            if (transformer.getSource() == '') {
                throw new IllegalArgumentException("Empty source in node ${node}")
            }
            if (transformer.getTarget() == '') {
                throw new IllegalArgumentException("Empty target in node ${node}")
            }

            def includes = [], excludes = []

            node.Filter.Include.each { includes.add(it.text()) }
            node.Filter.Exclude.each { excludes.add(it.text()) }

            includes.addAll(globalIncludes)
            excludes.addAll(globalExcludes)

            transformer.classFilterSpec.addIncludes(includes)
            transformer.classFilterSpec.addExcludes(excludes)
            transformers.add(transformer)
    }

    DroidAssistConfiguration(Project project) {
        this.project = project
    }

    List<Transformer> parserFrom(File file) {
        def transformers = parse(file)
        return transformers
    }

    List<Transformer> parse(File file) {
        Node configs = new XmlParser(true, true, true).parse(file)

        configs.Global.Filter.Include.each { globalIncludes.add(it.text()) }
        configs.Global.Filter.Exclude.each { globalExcludes.add(it.text()) }

        configs.Replace.MethodCall.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodCallReplaceTransformer()
                }
        }
        configs.Replace.MethodExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodExecutionReplaceTransformer()
                }
        }

        configs.Replace.ConstructorCall.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorCallReplaceTransformer()
                }
        }
        configs.Replace.ConstructorExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorExecutionReplaceTransformer()
                }
        }

        configs.Replace.InitializerExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(INITIALIZER, node) {
                    return new InitializerExecutionReplaceTransformer()
                }
        }

        configs.Replace.FieldRead.each {
            node ->
                sourceTargetTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessReplaceTransformer().setFieldWrite(false)
                }
        }
        configs.Replace.FieldWrite.each {
            node ->
                sourceTargetTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessReplaceTransformer().setFieldWrite(true)
                }
        }

        //insert
        //
        //before call
        configs.Insert.BeforeMethodCall.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodCallInsertTransformer().setAsBefore(true)
                }
        }
        configs.Insert.BeforeConstructorCall.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorCallInsertTransformer().setAsBefore(true)
                }
        }

        //before execution
        configs.Insert.BeforeMethodExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodExecutionInsertTransformer().setAsBefore(true)
                }
        }
        configs.Insert.BeforeConstructorExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorExecutionInsertTransformer().setAsBefore(true)
                }
        }
        configs.Insert.BeforeInitializerExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(INITIALIZER, node) {
                    return new InitializerExecutionInsertTransformer().setAsBefore(true)
                }
        }
        configs.Insert.BeforeFieldRead.each {
            node ->
                sourceTargetTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessInsertTransformer().setFieldWrite(false).setAsBefore(true)
                }
        }
        configs.Insert.BeforeFieldWrite.each {
            node ->
                sourceTargetTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessInsertTransformer().setFieldWrite(true).setAsBefore(true)
                }
        }

        //insert
        //after call
        configs.Insert.AfterMethodCall.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodCallInsertTransformer().setAsAfter(true)
                }
        }
        configs.Insert.AfterConstructorCall.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorCallInsertTransformer().setAsAfter(true)
                }
        }

        //after execution
        configs.Insert.AfterMethodExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodExecutionInsertTransformer().setAsAfter(true)
                }
        }
        configs.Insert.AfterConstructorExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorExecutionInsertTransformer().setAsAfter(true)
                }
        }
        //after
        configs.Insert.AfterInitializerExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(INITIALIZER, node) {
                    return new InitializerExecutionInsertTransformer().setAsAfter(true)
                }
        }

        configs.Insert.AfterFieldRead.each {
            node ->
                sourceTargetTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessInsertTransformer().setFieldWrite(false).setAsAfter(true)
                }
        }
        configs.Insert.AfterFieldWrite.each {
            node ->
                sourceTargetTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessInsertTransformer().setFieldWrite(true).setAsAfter(true)
                }
        }

        //around
        configs.Around.MethodCall.each {
            node ->
                aroundTransformerNodeHandler(METHOD, node) {
                    return new MethodCallAroundTransformer()
                }
        }
        configs.Around.ConstructorCall.each {
            node ->
                aroundTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorCallAroundTransformer()
                }
        }
        configs.Around.MethodExecution.each {
            node ->
                aroundTransformerNodeHandler(METHOD, node) {
                    return new MethodExecutionAroundTransformer()
                }
        }
        configs.Around.ConstructorExecution.each {
            node ->
                aroundTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorExecutionAroundTransformer()
                }
        }
        configs.Around.InitializerExecution.each {
            node ->
                aroundTransformerNodeHandler(INITIALIZER, node) {
                    return new InitializerExecutionAroundTransformer()
                }
        }
        configs.Around.FieldRead.each {
            node ->
                aroundTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessAroundTransformer().setFieldWrite(false)
                }
        }
        configs.Around.FieldWrite.each {
            node ->
                aroundTransformerNodeHandler(FIELD, node) {
                    return new FieldAccessAroundTransformer().setFieldWrite(true)
                }
        }

        //add
        //addcatch
        configs.Enhance.TryCatchMethodCall.each {
            node ->
                addCatchTransformerNodeHandler(METHOD, node) {
                    return new MethodCallTryCatchTransformer()
                }
        }
        configs.Enhance.TryCatchMethodExecution.each {
            node ->
                addCatchTransformerNodeHandler(METHOD, node) {
                    return new MethodExecutionTryCatchTransformer()
                }
        }
        configs.Enhance.TryCatchConstructorCall.each {
            node ->
                addCatchTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorCallTryCatchTransformer()
                }
        }
        configs.Enhance.TryCatchConstructorExecution.each {
            node ->
                addCatchTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorExecutionTryCatchTransformer()
                }
        }
        configs.Enhance.TryCatchInitializerExecution.each {
            node ->
                addCatchTransformerNodeHandler(INITIALIZER, node) {
                    return new InitializerExecutionTryCatchTransformer()
                }
        }

        //timing
        configs.Enhance.TimingMethodCall.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodCallTimingTransformer()
                }
        }
        configs.Enhance.TimingMethodExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(METHOD, node) {
                    return new MethodExecutionTimingTransformer()
                }
        }
        configs.Enhance.TimingConstructorCall.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorCallTimingTransformer()
                }
        }
        configs.Enhance.TimingConstructorExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(CONSTRUCTOR, node) {
                    return new ConstructorExecutionTimingTransformer()
                }
        }
        configs.Enhance.TimingInitializerExecution.each {
            node ->
                sourceTargetTransformerNodeHandler(INITIALIZER, node) {
                    return new InitializerExecutionTimingTransformer()
                }
        }
        configs.Enhance.ReparentClass.each {
            node ->
                sourceTargetTransformerNodeHandler(INITIALIZER, node) {
                    return new ReparentClassTransformer()
                }
        }
        return transformers
    }
}
