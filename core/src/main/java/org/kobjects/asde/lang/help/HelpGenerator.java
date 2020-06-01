package org.kobjects.asde.lang.help;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Text;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.StaticProperty;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Collection;

public class HelpGenerator {

  public static Text renderHelp(Program program, Object o) {
    if (o == null) {
      Collection<Property> all = program.mainModule.getBuiltinProperties();

      HelpGenerator generator = new HelpGenerator("Help");

      generator.addSubset(all, "Constants", s -> !(s.getStaticValue() instanceof Callable) && !(s.getType() instanceof MetaType));

      generator.addSubset(all, "Classes", s -> s.getStaticValue() instanceof Classifier);

      generator.addSubset(all,"Enums", s -> s.getStaticValue() instanceof EnumType);

      generator.addSubset(all, "Functions", s -> s.getStaticValue() instanceof Callable);

      return generator.textBuilder.build();

    }

    return renderObject(o);
  }




  public static Text renderObject(Object o) {
    if (o instanceof Property) {
      Property symbol = (Property) o;
      Object staticValue = symbol.isInstanceField() ? null : symbol.getStaticValue();
      if (staticValue instanceof Callable) {
        return renderFunction(symbol);
      }
      if (staticValue instanceof Type) {
        return renderObject(symbol.getStaticValue());
      }
      return renderProperty((Property) o);

    }
    if (o instanceof Classifier) {
      return renderClass((Classifier) o);
    }
    if (o instanceof EnumType) {
      return renderEnum((EnumType) o);
    }

    HelpGenerator generator = new HelpGenerator("Unknown");
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(o));
      generator.addParagraph(asb.build());

      return generator.textBuilder.build();
  }

  private static Text renderMethod(Property o) {
    HelpGenerator generator = new HelpGenerator("Method " + o.getName());
    generator.addSignaure(o.getName(), (FunctionType) o.getType());
    return generator.textBuilder.build();
  }



  private static Text renderProperty(Property o) {
    if (isMethod(o)) {
      return renderMethod(o);
    }

    HelpGenerator generator = new HelpGenerator("Property " + o.getName());

    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    asb.append("Type: ");
    generator.appendLink(asb, o.getType());

    generator.addParagraph(asb.build());

    generator.addParagraph(o.getDocumentation());

    return generator.textBuilder.build();
  }



  Text.Builder textBuilder = new Text.Builder();

  HelpGenerator(String title) {
    textBuilder.setTitle(title);
  }


  void appendLink(AnnotatedStringBuilder asb, Object linked) {
    if (linked instanceof StaticProperty) {
      StaticProperty symbol = (StaticProperty) linked;
      if (symbol.getStaticValue() instanceof Callable) {
        appendFunctionLink(asb, (StaticProperty) linked);
      } else if (symbol.getStaticValue() instanceof Type) {
        appendLink(asb, symbol.getStaticValue());
      } else {
        asb.append(symbol.getName());
        asb.append(": ");
        appendLink(asb, symbol.getType());
      }
    } else if (linked instanceof Property) {
      appendPropertyLink(asb, (Property) linked);
    } else {
      appendLink(asb, String.valueOf(linked), linked);
    }
  }


  private void appendMethodLink(AnnotatedStringBuilder asb, Property property) {
    appendLink(asb, property.getName(), property);
    appendShortSignature(asb, (FunctionType) property.getType());
  }

  private void appendPropertyLink(AnnotatedStringBuilder asb, Property property) {
    if (isMethod(property)) {
      appendMethodLink(asb, property);
    } else {
      appendLink(asb, property.getName(), property);
      asb.append(": ");
      asb.append(String.valueOf(property.getType()));
    }

  }

  void appendLink(AnnotatedStringBuilder asb, String text, Object linked) {
    asb.append(text, linked);
  }

  void addSignaure(String name, FunctionType functionType) {

    addSubtitle("Parameter");

    for (int i = 0; i < functionType.getParameterCount(); i++) {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(i + 1)).append(") ");
      appendLink(asb, functionType.getParameterType(i));
      addParagraph(asb.build());
    }

    if (functionType.getReturnType() != Types.VOID) {
      addSubtitle("Return Type");

      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append("  ");
      appendLink(asb, functionType.getReturnType());
      addParagraph(asb.build());
    }
  }

  void appendShortSignature(AnnotatedStringBuilder asb, FunctionType functionType) {
    asb.append('(');
    for (int i = 0; i < functionType.getParameterCount(); i++) {
      if (i > 0) {
        asb.append(", ");
      }
      asb.append(functionType.getParameterType(i).toString());
    }
    asb.append(")");

    if (functionType.getReturnType() != Types.VOID) {
      asb.append(" -> ").append(functionType.getReturnType().toString());
    }
  }

  void appendFunctionLink(AnnotatedStringBuilder sb, Property functionSymbol) {
    Callable function = (Callable) functionSymbol.getStaticValue();

    appendLink(sb, functionSymbol.getName(), functionSymbol);

    appendShortSignature(sb, function.getType());
  }

  private void addSubtitle(String text) {
    textBuilder.addSubtitle(text);
  }


  void addSubset(Collection<Property> all, String subtitle, Predicate<Property> filter) {
    addSubtitle(subtitle);

    for (Property s : all) {
      if (filter.test(s)) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        appendLink(asb, s);
        addParagraph(asb.build());
      }
    }
  }


  static Text renderFunction(Property functionSymbol) {
    HelpGenerator generator = new HelpGenerator("Function " + functionSymbol.getName());

    Callable function = (Callable) functionSymbol.getStaticValue();

    generator.addSignaure(functionSymbol.getName(), function.getType());

    CharSequence documentation = function.getDocumentation();
    if (documentation != null) {
      generator.addSubtitle("Description");
      generator.addParagraph(documentation);
    }

    return generator.textBuilder.build();
  }


  static Text renderEnum(EnumType enumType) {
    HelpGenerator generator = new HelpGenerator("Enum " + enumType.toString());

    generator.addSubtitle("Literals");

    for (Object literal : enumType.literals) {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(literal));
      generator.addParagraph(asb.build());
    }
    return generator.textBuilder.build();
  }


  static Text renderClass(Classifier classifier) {
    HelpGenerator generator = new HelpGenerator("Class " + classifier.toString());

    generator.addParagraph(AnnotatedString.of(classifier.getDocumentation()));

    for (boolean methods : new boolean[]{false, true}) {

      boolean first = true;

      for (Property descriptor : classifier.getProperties()) {
        if (isMethod(descriptor) == methods) {
          if (first) {
            generator.addSubtitle(methods ? "Methods" : "Properties");
            first = false;
          }
          AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
          generator.appendPropertyLink(asb, descriptor);
          generator.addParagraph(asb.build());
        }
      }
    }
    return generator.textBuilder.build();
  }

  private static  boolean isMethod(Property descriptor) {
    return descriptor.getType() instanceof FunctionType && !(descriptor.getType() instanceof ListType);
  }


  private void addParagraph(CharSequence charSequence) {
    if (charSequence != null) {
      textBuilder.addParagraph(charSequence);
    }
  }


  interface Predicate<T> {
    boolean test(T value);
  }
}
