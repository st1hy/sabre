package com.github.st1hy.sabre.image.inject;


import com.github.st1hy.sabre.core.injector.component.ActivityComponent;
import com.github.st1hy.sabre.image.ImageActivity;

import dagger.Component;

@PerImageActivity
@Component(dependencies = ActivityComponent.class, modules = ImageActivityModule.class)
public interface ImageActivityComponent {
    void inject(ImageActivity activity);
}
