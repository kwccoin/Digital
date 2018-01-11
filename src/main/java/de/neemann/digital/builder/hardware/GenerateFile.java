package de.neemann.digital.builder.hardware;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.builder.PinMapException;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.table.BuilderExpressionCreator;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.LineBreaker;
import de.neemann.gui.MyFileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Generates a file. Used for JEDEC and TT" generation
 */
public class GenerateFile implements HardwareDescriptionGenerator {

    private final String suffix;
    private final ExpressionToFileExporter expressionExporter;
    private final String path;
    private final String description;

    /**
     * Creates a new instance.
     *
     * @param suffix             the file suffix
     * @param expressionExporter the exporter
     * @param path               then menu path
     * @param description        the description, used as a tool tip
     */
    public GenerateFile(String suffix, ExpressionToFileExporter expressionExporter, String path, String description) {
        this.suffix = suffix;
        this.expressionExporter = expressionExporter;
        this.path = path;
        this.description = description;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void create(JDialog parent, File circuitFile, TruthTable table, ExpressionListenerStore expressions) throws Exception {
        if (circuitFile == null)
            circuitFile = new File("circuit." + suffix);
        else
            circuitFile = SaveAsHelper.checkSuffix(circuitFile, suffix);

        JFileChooser fileChooser = new MyFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JEDEC", suffix));
        fileChooser.setSelectedFile(circuitFile);
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try {
                expressionExporter.getPinMapping().addAll(table.getPins());
                expressionExporter.getPinMapping().setClockPin(table.getClockPinInt());
                new BuilderExpressionCreator(expressionExporter.getBuilder(), ExpressionModifier.IDENTITY).create(expressions);
                expressionExporter.export(SaveAsHelper.checkSuffix(fileChooser.getSelectedFile(), suffix));
            } catch (ExpressionException | FormatterException | IOException | FuseMapFillerException | PinMapException e) {
                new ErrorMessage(Lang.get("msg_errorDuringHardwareExport")).addCause(e).show(parent);
            }
        }

        ArrayList<String> pinsWithoutNumber = table.getPinsWithoutNumber();
        if (pinsWithoutNumber != null)
            JOptionPane.showMessageDialog(parent,
                    new LineBreaker().toHTML().breakLines(Lang.get("msg_thereAreMissingPinNumbers", pinsWithoutNumber)),
                    Lang.get("msg_warning"),
                    JOptionPane.WARNING_MESSAGE);
    }
}
