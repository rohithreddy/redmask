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

public class BigIntRangeMasking extends MaskingRuleDef {

  public BigIntRangeMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public BigIntRangeMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskBigIntRange(config));
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName) throws IOException, TemplateException {

    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    if (validateAndAddParameters(paramsList)) {
      return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_BIGINT_RANGE_FUNC, paramsList);
    }
    return this.getColumnName();
  }

  protected boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("step")) {
      int step = Integer.parseInt(this.getMaskParams().get("step"));
      if (step > 0) {
        parameters.add(String.valueOf(step));
        return true;
      }
    }
    return false;
  }
}
