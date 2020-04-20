package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class FixedSizeIntegerMasking extends MaskingRuleDef {

  public FixedSizeIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public FixedSizeIntegerMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskIntegerInRange(config));
    funcSet.add(MaskingQueryUtil.maskIntegerFixedSize(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return "redmask.generate(" + this.getColumnName() + ",5) as " + this.getColumnName();
  }
}
