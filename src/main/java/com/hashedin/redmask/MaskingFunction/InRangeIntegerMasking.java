package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InRangeIntegerMasking extends MaskingRuleDef {

  public InRangeIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  InRangeIntegerMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskIntegerInRange(config));
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName) throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    if (validateAndAddParameters(paramsList)) {
      return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC, paramsList);
    }
    return this.getColumnName();
  }

  protected boolean validateAndAddParameters(List<String> parameters) {
    if ((this.getMaskParams().containsKey("min")) && (this.getMaskParams().containsKey("max"))) {
      int min = Integer.parseInt(this.getMaskParams().get("min"));
      int max = Integer.parseInt(this.getMaskParams().get("max"));
      if (max > min) {
        parameters.add(String.valueOf(min));
        parameters.add(String.valueOf(max));
        return true;
      }
    }
    return false;
  }
}
