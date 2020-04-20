package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CardMasking extends MaskingRuleDef {

  public CardMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public CardMasking() {
  }

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskNumbers(config));
    funcSet.add(MaskingQueryUtil.maskCard(config));
  }

  @Override
  public String getSubQuery(MaskConfiguration config, String tableName) throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    if (validateAndAddParameters(paramsList))
      return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_CARD_FUNC, paramsList);
    return this.getColumnName();

  }

  @Override
  protected boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("separator") && this.getMaskParams().containsKey("val1")
        && this.getMaskParams().containsKey("val2")) {
      String separator = this.getMaskParams().get("separator");
      int val1 = Integer.parseInt(this.getMaskParams().get("val1"));
      int val2 = Integer.parseInt(this.getMaskParams().get("val2"));

      if ((val1 < 0) || (val2 < 0))
        return false;

      switch (this.getMaskType()) {
        case CREDIT_CARD_SHOW_FIRST:
          parameters.add("'first'");
          break;

        case CREDIT_CARD_SHOW_LAST:
          parameters.add("last");
          break;

        case CREDIT_CARD_SHOW_FIRST_LAST:
          parameters.add("'firstnlast'");
          break;
      }
      parameters.add(separator);
      parameters.add(String.valueOf(val1));
      parameters.add(String.valueOf(val2));

      return true;
    } else
      return false;


  }

}