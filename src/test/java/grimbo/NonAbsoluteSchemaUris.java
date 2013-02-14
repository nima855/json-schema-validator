/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grimbo;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.cfg.LoadingConfiguration;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.library.DraftV3Library;
import com.github.fge.jsonschema.load.SchemaLoader;
import com.github.fge.jsonschema.processing.Processor;
import com.github.fge.jsonschema.processing.ProcessorChain;
import com.github.fge.jsonschema.processors.data.FullValidationContext;
import com.github.fge.jsonschema.processors.data.ValidationData;
import com.github.fge.jsonschema.processors.ref.RefResolverProcessor;
import com.github.fge.jsonschema.processors.validation.ValidationChain;
import com.github.fge.jsonschema.processors.validation.ValidationProcessor;
import com.github.fge.jsonschema.report.ListProcessingReport;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.tree.JsonTree;
import com.github.fge.jsonschema.tree.SchemaTree;
import com.github.fge.jsonschema.tree.SimpleJsonTree;
import com.github.fge.jsonschema.util.JsonLoader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static org.testng.Assert.*;

public final class NonAbsoluteSchemaUris
{
    private ValidationProcessor validator;
    private SchemaTree tree;

    @BeforeMethod
    public void beforeTest()
        throws ProcessingException
    {
        final ValidationChain chain = new ValidationChain(DraftV3Library.get());
        final LoadingConfiguration cfg = LoadingConfiguration.newConfiguration()
            .setNamespace("resource:/grimbo/").freeze();
        final SchemaLoader loader = new SchemaLoader(cfg);
        final RefResolverProcessor refResolver
            = new RefResolverProcessor(loader);
        tree = loader.get(URI.create("child1/child.json"));
        final Processor<ValidationData, FullValidationContext> processor
            = ProcessorChain.startWith(refResolver).chainWith(chain).getProcessor();
        validator = new ValidationProcessor(processor);
    }

    @Test
    public void testEmptyObject()
        throws IOException, ProcessingException
    {
        final JsonNode data
            = JsonLoader.fromResource("/grimbo/empty-object.json");
        final JsonTree instance = new SimpleJsonTree(data);
        final ValidationData validationData
            = new ValidationData(tree, instance);

        final ListProcessingReport listReport = new ListProcessingReport();
        final ProcessingReport out
            = validator.process(listReport, validationData);
        assertFalse(out.isSuccess());
    }

    @Test
    public void testTestObject()
        throws IOException, ProcessingException
    {
        final JsonNode data
            = JsonLoader.fromResource("/grimbo/test-object.json");
        final JsonTree instance = new SimpleJsonTree(data);
        final ValidationData validationData
            = new ValidationData(tree, instance);

        final ListProcessingReport listReport = new ListProcessingReport();
        final ProcessingReport out
            = validator.process(listReport, validationData);

        assertTrue(out.isSuccess());
    }

    @Test
    public void testTestObjectNoBodyItem()
        throws IOException, ProcessingException
    {
        final JsonNode data
            = JsonLoader.fromResource("/grimbo/test-object-no-bodyItem.json");
        final JsonTree instance = new SimpleJsonTree(data);
        final ValidationData validationData
            = new ValidationData(tree, instance);

        final ListProcessingReport listReport = new ListProcessingReport();
        final ProcessingReport out
            = validator.process(listReport, validationData);
        assertFalse(out.isSuccess());
    }
}
