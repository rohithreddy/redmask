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

public class StringMasking extends MaskingRuleDef {

  public StringMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public StringMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskString(config));
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    if (validateAndAddParameters(paramsList)) {
      return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_STRING_FUNC, paramsList);
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("separator") && this.getMaskParams().containsKey("prefix")
        && this.getMaskParams().containsKey("suffix")) {
      String separator = this.getMaskParams().get("separator");
      int prefix = Integer.parseInt(this.getMaskParams().get("prefix"));
      int suffix = Integer.parseInt(this.getMaskParams().get("suffix"));

      if ((prefix < 0) || (suffix < 0)) {
        return false;
      }
      parameters.add(separator);
      parameters.add(String.valueOf(prefix));
      parameters.add(String.valueOf(suffix));
    }
    return false;
  }

}