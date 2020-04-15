package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Set;

public class EmailMasking extends ColumnRule {

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskString(config));
    funcSet.add(MaskingFunctionQuery.maskEmail(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    switch (this.getMaskType()) {
      case EMAIL_SHOW_DOMAIN:
        return " redmask.emailmask(" + this.getName() + ")" + " as " + this.getName();

      case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        return " redmask.emailmask(" + this.getName() + ",'firstndomain') as " + this.getName();

      case EMAIL_SHOW_FIRST_CHARACTERS:
        return " redmask.emailmask(" + this.getName() + ",'firstN') as " + this.getName();

      case EMAIL_MASK_ALPHANUMERIC:
        return " redmask.emailmask(" + this.getName() + ",'nonspecialcharacter') as " + this.getName();

      default:
        return this.getName();
    }
  }
}
