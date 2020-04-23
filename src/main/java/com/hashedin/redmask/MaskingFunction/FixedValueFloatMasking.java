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

public class FixedValueFloatMasking extends MaskingRuleDef {

  public FixedValueFloatMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public FixedValueFloatMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskFloatFixedValue(config));
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName) throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    if (this.validateAndAddParameters(paramsList)) {
      return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_FLOAT_FIXED_VALUE_FUNC, paramsList);
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("value")) {
      // to assure the value is of Float type
      Float value = Float.parseFloat(this.getMaskParams().get("value"));
      parameters.add(String.valueOf(value));
      return true;
    }
    return false;
  }
}
