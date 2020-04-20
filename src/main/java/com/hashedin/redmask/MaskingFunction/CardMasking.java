package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CardMasking extends MaskingRuleDef {

  public CardMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public CardMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskNumbers(config));
    funcSet.add(MaskingQueryUtil.maskCard(config));
  }

  @Override
  public String getSubQuery(String tableName) {

    // TODO: Add validation of extra params and remove this hardcoded params.
    switch (this.getMaskType()) {
      case CREDIT_CARD_SHOW_FIRST:
        return "redmask.cardmask(" + this.getColumnName() + ",'first') as " + this.getColumnName();

      case CREDIT_CARD_SHOW_LAST:
        return " redmask.cardmask(" + this.getColumnName() + ") as " + this.getColumnName();

      case CREDIT_CARD_SHOW_FIRST_LAST: {
        return " redmask.cardmask(" + this.getColumnName() + ",'firstnlast','',4,4) as " + this.getColumnName();
      }
    }
    return this.getColumnName();
  }
}