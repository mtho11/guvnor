/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.ide.common.assistant.refactor;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.common.assistant.info.RuleRefactorInfo;
import org.drools.ide.common.assistant.info.drl.DRLContentTypeEnum;
import org.drools.ide.common.assistant.info.drl.DRLRuleRefactorInfo;
import org.drools.ide.common.assistant.info.drl.RuleBasicContentInfo;
import org.drools.ide.common.assistant.option.AssistantOption;
import org.drools.ide.common.assistant.option.RenameAssistantOption;
import org.drools.ide.common.assistant.option.ReplaceAssistantOption;
import org.drools.ide.common.assistant.refactor.drl.FixImport;
import org.drools.ide.common.assistant.refactor.drl.VariableBinding;
import org.drools.ide.common.assistant.refactor.drl.VariableRename;

public class DRLRuleRefactor extends AbstractRuleRefactor {

    public DRLRuleRefactor(RuleRefactorInfo refactorInfo) {
        this.ruleRefactorInfo = refactorInfo;
        this.options = new ArrayList<AssistantOption>();
    }

    @Override
    public List<AssistantOption> execute(int offset) {
        this.offset = offset;
        RuleBasicContentInfo contentInfo = ((DRLRuleRefactorInfo)ruleRefactorInfo).getContentAt(offset);
        if (contentInfo==null)
            return this.options;
        if ((option = this.bindVariable(contentInfo))!=null)
            this.options.add(option);
        if ((option = this.fixImports(contentInfo))!=null)
            this.options.add(option);
        if ((option = this.renameVariable(contentInfo))!=null)
            this.options.add(option);
        return this.options;
    }

    @Override
    protected AssistantOption bindVariable(RuleBasicContentInfo contentInfo) {
        if (contentInfo==null) return null;
        if (!contentInfo.getType().equals(DRLContentTypeEnum.RULE_LHS_LINE))
            return null;
        String response = VariableBinding.execute(contentInfo, offset-contentInfo.getOffset());
        if (response.equals(contentInfo.getContent()))
            return null;
        return new ReplaceAssistantOption("assign to variable", response, contentInfo.getOffset(), contentInfo.getContentLength(), offset);
    }

    @Override
    protected AssistantOption fixImports(RuleBasicContentInfo contentInfo) {
        if (contentInfo==null) return null;
        if (!contentInfo.getType().equals(DRLContentTypeEnum.RULE_LHS_LINE) &&
                !contentInfo.getType().equals(DRLContentTypeEnum.RULE_RHS_LINE))
            return null;
        List<RuleBasicContentInfo> imports = ((DRLRuleRefactorInfo)ruleRefactorInfo).getImports();
        FixImport.execute(contentInfo, imports);
        return null;
    }

    @Override
    protected AssistantOption renameVariable(RuleBasicContentInfo contentInfo) {
        if (contentInfo==null) return null;
        if (!contentInfo.getType().equals(DRLContentTypeEnum.RULE_LHS_LINE) &&
                !contentInfo.getType().equals(DRLContentTypeEnum.RULE_RHS_LINE))
            return null;
        String variable;
        if ((variable = VariableRename.isPossible(contentInfo, offset-contentInfo.getOffset()))!=null)
            return new RenameAssistantOption("rename variable", variable, contentInfo, contentInfo.getOffset());
        return null;
    }

}
