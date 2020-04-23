package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.InvalidParameterValueException;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.MissingParameterException;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StringMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);


  public StringMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public StringMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskString(config));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName) throws MissingParameterException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_STRING_FUNC, paramsList);
      } else {
        throw new MissingParameterException("Expected parameters: separator, prefix, suffix ");
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("separator") && this.getMaskParams().containsKey("prefix")
        && this.getMaskParams().containsKey("suffix")) {
      String separator = this.getMaskParams().get("separator");
      int prefix = Integer.parseInt(this.getMaskParams().get("prefix"));
      int suffix = Integer.parseInt(this.getMaskParams().get("suffix"));

      if (prefix < 0) {
        throw new InvalidParameterValueException("\'prefix\' value should be greater than or equal to 0");
      }
      if (suffix < 0) {
          throw new InvalidParameterValueException("\'suffix\' value should be greater than or equal to 0");
      }
      parameters.add(separator);
      parameters.add(String.valueOf(prefix));
      parameters.add(String.valueOf(suffix));
    }
    return false;
  }

}