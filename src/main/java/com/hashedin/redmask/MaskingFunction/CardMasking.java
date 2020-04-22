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

public class CardMasking extends MaskingRuleDef {
  private static final Logger log = LogManager.getLogger(BigIntRangeMasking.class);


  public CardMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public CardMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskNumbers(config));
      funcSet.add(MaskingQueryUtil.maskCard(config));
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
        return MaskingQueryUtil.processQueryTemplate(config, MaskingConstants.MASK_CARD_FUNC, paramsList);
      } else {
        throw new MissingParameterException("Expected parameters: separator, val1, val2 ");
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();

  }

  protected boolean validateAndAddParameters(List<String> parameters) {
    if (this.getMaskParams().containsKey("separator") && this.getMaskParams().containsKey("val1")
        && this.getMaskParams().containsKey("val2")) {
      String separator = this.getMaskParams().get("separator");
      int val1 = Integer.parseInt(this.getMaskParams().get("val1"));
      int val2 = Integer.parseInt(this.getMaskParams().get("val2"));

      if (val1 < 0) {
        throw new InvalidParameterValueException("\'val1\' value should be greater than or equal to 0");
      }

      if (val2 < 0) {
          throw new InvalidParameterValueException("\'val2\' value should be greater than or equal to 0");
      }

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
        default:
          break;
      }
      parameters.add(separator);
      parameters.add(String.valueOf(val1));
      parameters.add(String.valueOf(val2));

      return true;
    } else {
      return false;
    }

  }

}