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

public class EmailMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);


  public EmailMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public EmailMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskString(config));
      funcSet.add(MaskingQueryUtil.maskEmail(config));
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
        return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_EMAIL_FUNC, paramsList);
      } else {
        if (this.getMaskType() == MaskType.EMAIL_SHOW_FIRST_CHARACTERS) {
          throw new MissingParameterException("Expected parameters:  val");
        }
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  protected boolean validateAndAddParameters(List<String> parameters) {
    switch (this.getMaskType()) {
      case EMAIL_SHOW_DOMAIN:
        parameters.add("'domain'");
        break;

      case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        parameters.add("'firstndomain'");
        break;

      case EMAIL_SHOW_FIRST_CHARACTERS:
        parameters.add("'firstN'");
        if (this.getMaskParams().containsKey("val")) {
          int val = Integer.parseInt(this.getMaskParams().get("val"));
          if (val < 0) {
            throw new InvalidParameterValueException("\'val\' value should be greater than or equal to 0");
          }
          parameters.add(String.valueOf(val));
        } else {
          return false;
        }
        break;

      case EMAIL_MASK_ALPHANUMERIC:
        parameters.add("'nonspecialcharacter'");
        break;
      default:
        return false;
    }
    return true;
  }
}
