package com.hashedin.redmask.configurations;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

@JsonDeserialize(using = ColumnRuleDeserializer.class)
public abstract class ColumnRule implements Serializable {

  private String name;
  private MaskType maskType;
  private JsonNode maskParams;

  public ColumnRule() {
  }

  public abstract void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException;

  public abstract String getSubQuery(String tableName);

  public JsonNode getMaskParams() {
    return maskParams;
  }

  public void setMaskParams(JsonNode maskParams) {
    this.maskParams = maskParams;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MaskType getMaskType() {
    return maskType;
  }

  public void setMaskType(MaskType maskType) {
    this.maskType = maskType;
  }

  @Override
  public String toString() {
    return "ColumnRule [name=" + name + ", maskType=" + maskType + ", maskParams=" + maskParams + "]";
  }

}

class ColumnRuleDeserializer extends StdDeserializer<ColumnRule> {

  protected ColumnRuleDeserializer(Class<?> vc) {
    super(vc);
  }

  public ColumnRuleDeserializer() {
    this(null);
  }

  @Override
  public ColumnRule deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String name = node.get("name").asText();
    MaskType maskType = MaskType.valueOf(node.get("maskType").asText());
    JsonNode maskParams = node.get("maskParams");
    ColumnRule columnRule = new ColumnRule() {
      @Override
      public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) {
      }

      @Override
      public String getSubQuery(String tableName) {
        return null;
      }
    };
    columnRule.setName(name);
    columnRule.setMaskParams(maskParams);
    columnRule.setMaskType(maskType);
    return columnRule;
  }
}
