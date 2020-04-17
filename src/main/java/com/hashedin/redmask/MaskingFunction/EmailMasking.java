package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class EmailMasking extends MaskingRuleDef {

  public EmailMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public EmailMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskString(config));
    funcSet.add(MaskingFunctionQuery.maskEmail(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    switch (this.getMaskType()) {
      case EMAIL_SHOW_DOMAIN:
        return " redmask.emailmask(" + this.getColumnName() + ")" + " as " + this.getColumnName();

      case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        return " redmask.emailmask(" + this.getColumnName() + ",'firstndomain') as " + this.getColumnName();

      case EMAIL_SHOW_FIRST_CHARACTERS:
        return " redmask.emailmask(" + this.getColumnName() + ",'firstN') as " + this.getColumnName();

      case EMAIL_MASK_ALPHANUMERIC:
        return " redmask.emailmask(" + this.getColumnName() + ",'nonspecialcharacter') as " + this.getColumnName();

      default:
        return this.getColumnName();
    }
  }
}
