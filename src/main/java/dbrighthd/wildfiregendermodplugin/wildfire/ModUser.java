package dbrighthd.wildfiregendermodplugin.wildfire;

import dbrighthd.wildfiregendermodplugin.wildfire.setup.ModConfiguration;

import java.util.UUID;

public record ModUser(UUID userId,
                      ModConfiguration configuration) {
}
