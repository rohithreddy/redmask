package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Set;

public class CardMasking extends ColumnRule {
  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskNumbers(config));
    funcSet.add(MaskingFunctionQuery.maskCard(config));
  }

  @Override
  public String getSubQuery(String tableName) {

    switch (this.getMaskType()) {
      case CREDIT_CARD_SHOW_FIRST:
        return "redmask.cardmask(" + this.getName() + ",'first') as " + this.getName();

      case CREDIT_CARD_SHOW_LAST:
        return " redmask.cardmask(" + this.getName() + ") as " + this.getName();

      case CREDIT_CARD_SHOW_FIRST_LAST: {
        return " redmask.cardmask(" + this.getName() + ",'firstnlast','',4,4) as " + this.getName();
      }
    }
    return this.getName();
  }
}