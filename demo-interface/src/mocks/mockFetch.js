export default class MockFetch {
    constructor(payload) {
        let defaultPayload = {
            itemsResponse: {
                status: 200,
                body: [{
                    id: '12345',
                    name: 'name',
                    description: 'description',
                    amount: '1',
                }]
            },
            itemResponse: {
                status: 200,
                body: {
                    id: '12345',
                    name: 'name',
                    description: 'description',
                    amount: '1',
                }
            },
            isValidResponse: {
                status: 200,
                body: true
            }
        }
        this.payload = {...defaultPayload, ...payload};
    }

    execute = async (url, data) => {
        switch(data.method) {
            case 'GET':
                if (url.includes('/items/isValid/')) {
                    return {
                        status: this.payload.isValidResponse.status,
                        json: () => this.payload.isValidResponse.body,
                    }
                } else if (url.endsWith('/items')) {
                    return {
                        status: this.payload.itemsResponse.status,
                        json: () => this.payload.itemsResponse.body,
                    }
                } else if (url.includes('/items/')) {
                    return {
                        status: this.payload.itemResponse.status,
                        json: () => this.payload.itemResponse.body,
                    }
                }
                break

            case 'POST':
                if (url.includes('/item/')) {
                    return {
                        status: this.payload.itemResponse.status,
                        json: () => this.payload.itemResponse.body,
                    };
                }
                break

            case 'PUT':
                if (url.includes('/item/')) {
                    return {
                        ok: true,
                        status: this.payload.itemResponse.status,
                        json: () => this.payload.itemResponse.body,
                    }
                }
                break

            case 'DELETE':
                if (url.includes('/items/')) {
                    return {
                        status: 200,
                    };
                }
                break

            default:
                throw new Error(`Undefined fetch mock for url: ${url} and method: ${data.method}`);
        }
    }
}
