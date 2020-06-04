export class ServerDownController {
    constructor(ArtifactoryFeatures) {
        this.features = ArtifactoryFeatures;
        this.productName = this.features.isJCR() ? this.features.getGlobalName() : `JFrog ${this.features.getGlobalName()}`;
    }
}