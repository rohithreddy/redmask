package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import com.hashedin.redmask.service.QueryBuilderUtil;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmailMasking extends ColumnRule {

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskString(config));
    funcSet.add(MaskingFunctionQuery.maskEmail(config));
  }

  @Override
  public String getSubQuery(MaskConfiguration config, String tableName) throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getName());
    switch (this.getMaskType()) {
      case EMAIL_SHOW_DOMAIN:
        paramsList.add("'domain'");
        break;

      case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        paramsList.add("'firstndomain'");
        break;

      case EMAIL_SHOW_FIRST_CHARACTERS:
        paramsList.add("'firstN'");
        break;

      case EMAIL_MASK_ALPHANUMERIC:
        paramsList.add("'nonspecialcharacter'");
        break;

      default:
        return this.getName();
    }
    return QueryBuilderUtil.processQueryTemplate(config, MaskingConstants.MASK_EMAIL_FUNC, paramsList);
  }
}
